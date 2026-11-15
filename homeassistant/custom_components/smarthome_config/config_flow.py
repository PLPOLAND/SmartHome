"""Config flow and the management menu (options flow) of SmartHome Config."""

from __future__ import annotations

from typing import Any

import voluptuous as vol

from homeassistant.components import mqtt
from homeassistant.config_entries import (
    ConfigEntry,
    ConfigEntryState,
    ConfigFlow,
    ConfigFlowResult,
    OptionsFlow,
)
from homeassistant.core import callback
from homeassistant.helpers import area_registry as ar
from homeassistant.helpers.selector import (
    BooleanSelector,
    NumberSelector,
    NumberSelectorConfig,
    NumberSelectorMode,
    SelectOptionDict,
    SelectSelector,
    SelectSelectorConfig,
    SelectSelectorMode,
    TextSelector,
)

from .client import SmartHomeClient, SmartHomeError
from .const import (
    ACTION_TOGGLE,
    ADDABLE_SENSOR_TYPES,
    CLICK_ACTIONS,
    CONF_BASE_TOPIC,
    DEFAULT_BASE_TOPIC,
    DEVICE_TYPES,
    DOMAIN,
    MAX_CLICK_FUNCTIONS,
    NO_DEVICE,
    SENSOR_TYPE_BUTTON,
    SNAPSHOT_TIMEOUT,
    TYPE_BLIND,
)

PIN_SELECTOR = NumberSelector(
    NumberSelectorConfig(min=0, max=255, step=1, mode=NumberSelectorMode.BOX)
)


def _select(
    options: list[SelectOptionDict] | list[str],
    translation_key: str | None = None,
    custom_value: bool = False,
) -> SelectSelector:
    config = SelectSelectorConfig(
        options=options, mode=SelectSelectorMode.DROPDOWN, custom_value=custom_value
    )
    if translation_key is not None:
        config["translation_key"] = translation_key
    return SelectSelector(config)


class SmartHomeConfigFlow(ConfigFlow, domain=DOMAIN):
    """Connect to the SmartHome server through the MQTT integration."""

    VERSION = 1

    @staticmethod
    @callback
    def async_get_options_flow(config_entry: ConfigEntry) -> OptionsFlow:
        """Return the management menu."""
        return SmartHomeOptionsFlow()

    async def async_step_user(
        self, user_input: dict[str, Any] | None = None
    ) -> ConfigFlowResult:
        """Ask for the server's MQTT base topic and check that the server is there."""
        errors: dict[str, str] = {}
        if user_input is not None:
            base_topic = user_input[CONF_BASE_TOPIC].strip().strip("/")
            if not base_topic:
                errors[CONF_BASE_TOPIC] = "invalid_topic"
            elif not await mqtt.async_wait_for_mqtt_client(self.hass):
                errors["base"] = "mqtt_unavailable"
            else:
                client = SmartHomeClient(self.hass, base_topic)
                await client.async_start()
                try:
                    found = await client.async_wait_for_snapshot(SNAPSHOT_TIMEOUT)
                finally:
                    await client.async_stop()
                if found:
                    return self.async_create_entry(
                        title="SmartHome", data={CONF_BASE_TOPIC: base_topic}
                    )
                errors["base"] = "no_server"
        return self.async_show_form(
            step_id="user",
            data_schema=vol.Schema(
                {
                    vol.Required(
                        CONF_BASE_TOPIC,
                        default=(user_input or {}).get(CONF_BASE_TOPIC, DEFAULT_BASE_TOPIC),
                    ): TextSelector()
                }
            ),
            errors=errors,
        )


class SmartHomeOptionsFlow(OptionsFlow):
    """Management menu: rooms, devices, sensors, button click functions, maintenance."""

    def __init__(self) -> None:
        self._type: str | None = None
        self._target: dict[str, Any] | None = None
        self._result = ""

    # ------------------------------------------------------------------ helpers

    @property
    def _client(self) -> SmartHomeClient:
        return self.config_entry.runtime_data

    @property
    def _snapshot(self) -> dict[str, Any]:
        return self._client.snapshot or {}

    def _rooms(self) -> list[dict[str, Any]]:
        return sorted(self._snapshot.get("rooms", []), key=lambda room: room["name"].lower())

    def _room_name(self, room_id: int) -> str:
        for room in self._snapshot.get("rooms", []):
            if room["id"] == room_id:
                return room["name"]
        return "?"

    def _devices(self) -> list[dict[str, Any]]:
        return sorted(
            self._snapshot.get("devices", []),
            key=lambda dev: (self._room_name(dev["room"]).lower(), dev["name"].lower()),
        )

    def _sensors(self, only_type: str | None = None) -> list[dict[str, Any]]:
        return sorted(
            (
                sensor
                for sensor in self._snapshot.get("sensors", [])
                if only_type is None or sensor["type"] == only_type
            ),
            key=lambda sen: (self._room_name(sen["room"]).lower(), sen["name"].lower()),
        )

    def _room_selector(self) -> SelectSelector:
        return _select(
            [SelectOptionDict(value=str(room["id"]), label=room["name"]) for room in self._rooms()]
        )

    def _slave_selector(self) -> SelectSelector:
        """Detected slaves; a custom value allows a slave that is not connected right now."""
        slaves = {slave["address"]: slave["connected"] for slave in self._snapshot.get("slaves", [])}
        for item in [*self._snapshot.get("devices", []), *self._snapshot.get("sensors", [])]:
            slaves.setdefault(item["slave"], False)
        return _select(
            [
                SelectOptionDict(
                    value=str(address), label=f"{address} ({'online' if connected else 'offline'})"
                )
                for address, connected in sorted(slaves.items())
                if address >= 0
            ],
            custom_value=True,
        )

    def _item_selector(self, items: list[dict[str, Any]]) -> SelectSelector:
        return _select(
            [
                SelectOptionDict(
                    value=str(item["id"]),
                    label=f"{self._room_name(item['room'])} / {item['name']}",
                )
                for item in items
            ]
        )

    @staticmethod
    def _find(items: list[dict[str, Any]], item_id: str) -> dict[str, Any] | None:
        return next((item for item in items if str(item["id"]) == item_id), None)

    async def _request(
        self, action: str, payload: dict[str, Any], errors: dict[str, str]
    ) -> dict[str, Any] | None:
        """Send a request; on failure fill ``errors`` and return None."""
        self._error = ""
        try:
            return await self._client.async_request(action, payload)
        except SmartHomeError as err:
            if str(err) == "timeout":
                errors["base"] = "timeout"
            else:
                errors["base"] = "server_error"
                self._error = str(err)
            return None

    _error = ""

    def _form(
        self,
        step_id: str,
        schema: dict[Any, Any],
        errors: dict[str, str],
        placeholders: dict[str, str] | None = None,
    ) -> ConfigFlowResult:
        return self.async_show_form(
            step_id=step_id,
            data_schema=vol.Schema(schema),
            errors=errors,
            description_placeholders={"error": self._error, **(placeholders or {})},
        )

    @staticmethod
    def _parse_slave(user_input: dict[str, Any], errors: dict[str, str]) -> int | None:
        try:
            slave = int(str(user_input["slave"]).strip())
        except ValueError:
            slave = -1
        if slave < 0:
            errors["slave"] = "invalid_slave"
            return None
        return slave

    def _done(self, result: str) -> ConfigFlowResult:
        self._result = result
        self._target = None
        self._type = None
        return self.async_show_menu(
            step_id="done",
            menu_options=["init", "finish"],
            description_placeholders={"result": result},
        )

    # --------------------------------------------------------------------- menu

    async def async_step_init(
        self, user_input: dict[str, Any] | None = None
    ) -> ConfigFlowResult:
        """Show the main menu."""
        if self.config_entry.state is not ConfigEntryState.LOADED:
            return self.async_abort(reason="not_loaded")
        if self._client.snapshot is None and not await self._client.async_wait_for_snapshot(
            SNAPSHOT_TIMEOUT
        ):
            return self.async_abort(reason="no_server")
        return self.async_show_menu(
            step_id="init",
            menu_options=[
                "add_device",
                "edit_device",
                "remove_device",
                "add_sensor",
                "edit_sensor",
                "remove_sensor",
                "click_functions",
                "rooms",
                "maintenance",
            ],
        )

    async def async_step_done(
        self, user_input: dict[str, Any] | None = None
    ) -> ConfigFlowResult:
        """Shown after a successful change."""
        return self._done(self._result)

    async def async_step_finish(
        self, user_input: dict[str, Any] | None = None
    ) -> ConfigFlowResult:
        """Close the dialog."""
        return self.async_create_entry(data=dict(self.config_entry.options))

    # ------------------------------------------------------------------ devices

    async def async_step_add_device(
        self, user_input: dict[str, Any] | None = None
    ) -> ConfigFlowResult:
        """Pick the type of the new device (a blind needs a second pin)."""
        if user_input is not None:
            self._type = user_input["type"]
            return await self.async_step_add_device_details()
        return self._form(
            "add_device",
            {vol.Required("type", default=DEVICE_TYPES[0]): _select(DEVICE_TYPES, "device_type")},
            {},
        )

    async def async_step_add_device_details(
        self, user_input: dict[str, Any] | None = None
    ) -> ConfigFlowResult:
        """Name, room, slave and pins of the new device."""
        errors: dict[str, str] = {}
        is_blind = self._type == TYPE_BLIND
        if user_input is not None:
            slave = self._parse_slave(user_input, errors)
            if slave is not None:
                payload = {
                    "type": self._type,
                    "name": user_input["name"],
                    "room": int(user_input["room"]),
                    "slave": slave,
                    "pin": int(user_input["pin"]),
                }
                if is_blind:
                    payload["pin2"] = int(user_input["pin2"])
                data = await self._request("device/add", payload, errors)
                if data is not None:
                    return self._done(user_input["name"])
        values = user_input or {}
        schema: dict[Any, Any] = {
            vol.Required("name", default=values.get("name", vol.UNDEFINED)): TextSelector(),
            vol.Required("room", default=values.get("room", vol.UNDEFINED)): self._room_selector(),
            vol.Required("slave", default=values.get("slave", vol.UNDEFINED)): self._slave_selector(),
            vol.Required("pin", default=values.get("pin", vol.UNDEFINED)): PIN_SELECTOR,
        }
        if is_blind:
            schema[vol.Required("pin2", default=values.get("pin2", vol.UNDEFINED))] = PIN_SELECTOR
        return self._form("add_device_details", schema, errors)

    async def async_step_edit_device(
        self, user_input: dict[str, Any] | None = None
    ) -> ConfigFlowResult:
        """Pick the device to edit."""
        devices = self._devices()
        if not devices:
            return self.async_abort(reason="no_devices")
        if user_input is not None:
            self._target = self._find(devices, user_input["device"])
            if self._target is not None:
                return await self.async_step_edit_device_details()
        return self._form("edit_device", {vol.Required("device"): self._item_selector(devices)}, {})

    async def async_step_edit_device_details(
        self, user_input: dict[str, Any] | None = None
    ) -> ConfigFlowResult:
        """Edit name, room, slave and pins; only changed fields are sent."""
        assert self._target is not None
        device = self._target
        errors: dict[str, str] = {}
        is_blind = device["type"] == TYPE_BLIND
        if user_input is not None:
            slave = self._parse_slave(user_input, errors)
            if slave is not None:
                payload: dict[str, Any] = {"id": device["id"]}
                if user_input["name"] != device["name"]:
                    payload["name"] = user_input["name"]
                if int(user_input["room"]) != device["room"]:
                    payload["room"] = int(user_input["room"])
                if slave != device["slave"]:
                    payload["slave"] = slave
                if int(user_input["pin"]) != device.get("pin"):
                    payload["pin"] = int(user_input["pin"])
                if is_blind and int(user_input["pin2"]) != device.get("pin2"):
                    payload["pin2"] = int(user_input["pin2"])
                if len(payload) == 1:
                    return self._done(device["name"])
                data = await self._request("device/update", payload, errors)
                if data is not None:
                    return self._done(user_input["name"])
        values = user_input or {
            "name": device["name"],
            "room": str(device["room"]),
            "slave": str(device["slave"]),
            "pin": device.get("pin"),
            "pin2": device.get("pin2"),
        }
        schema: dict[Any, Any] = {
            vol.Required("name", default=values["name"]): TextSelector(),
            vol.Required("room", default=values["room"]): self._room_selector(),
            vol.Required("slave", default=values["slave"]): self._slave_selector(),
            vol.Required("pin", default=values["pin"]): PIN_SELECTOR,
        }
        if is_blind:
            schema[vol.Required("pin2", default=values["pin2"])] = PIN_SELECTOR
        return self._form("edit_device_details", schema, errors, {"name": device["name"]})

    async def async_step_remove_device(
        self, user_input: dict[str, Any] | None = None
    ) -> ConfigFlowResult:
        """Remove a device."""
        devices = self._devices()
        if not devices:
            return self.async_abort(reason="no_devices")
        errors: dict[str, str] = {}
        if user_input is not None:
            device = self._find(devices, user_input["device"])
            if not user_input.get("confirm"):
                errors["confirm"] = "confirm_required"
            elif device is not None:
                data = await self._request("device/remove", {"id": device["id"]}, errors)
                if data is not None:
                    return self._done(device["name"])
        return self._form(
            "remove_device",
            {
                vol.Required(
                    "device", default=(user_input or {}).get("device", vol.UNDEFINED)
                ): self._item_selector(devices),
                vol.Required("confirm", default=False): BooleanSelector(),
            },
            errors,
        )

    # ------------------------------------------------------------------ sensors

    async def async_step_add_sensor(
        self, user_input: dict[str, Any] | None = None
    ) -> ConfigFlowResult:
        """Pick the type of the new sensor (thermometers are detected by the server)."""
        if user_input is not None:
            self._type = user_input["type"]
            return await self.async_step_add_sensor_details()
        return self._form(
            "add_sensor",
            {
                vol.Required("type", default=SENSOR_TYPE_BUTTON): _select(
                    ADDABLE_SENSOR_TYPES, "sensor_type"
                )
            },
            {},
        )

    async def async_step_add_sensor_details(
        self, user_input: dict[str, Any] | None = None
    ) -> ConfigFlowResult:
        """Name, room and slave of the new sensor; a button also has a pin."""
        errors: dict[str, str] = {}
        is_button = self._type == SENSOR_TYPE_BUTTON
        if user_input is not None:
            slave = self._parse_slave(user_input, errors)
            if slave is not None:
                payload = {
                    "type": self._type,
                    "name": user_input["name"],
                    "room": int(user_input["room"]),
                    "slave": slave,
                }
                if is_button:
                    payload["pin"] = int(user_input["pin"])
                data = await self._request("sensor/add", payload, errors)
                if data is not None:
                    return self._done(user_input["name"])
        values = user_input or {}
        schema: dict[Any, Any] = {
            vol.Required("name", default=values.get("name", vol.UNDEFINED)): TextSelector(),
            vol.Required("room", default=values.get("room", vol.UNDEFINED)): self._room_selector(),
            vol.Required("slave", default=values.get("slave", vol.UNDEFINED)): self._slave_selector(),
        }
        if is_button:
            schema[vol.Required("pin", default=values.get("pin", vol.UNDEFINED))] = PIN_SELECTOR
        return self._form("add_sensor_details", schema, errors)

    async def async_step_edit_sensor(
        self, user_input: dict[str, Any] | None = None
    ) -> ConfigFlowResult:
        """Pick the sensor to edit."""
        sensors = self._sensors()
        if not sensors:
            return self.async_abort(reason="no_sensors")
        if user_input is not None:
            self._target = self._find(sensors, user_input["sensor"])
            if self._target is not None:
                return await self.async_step_edit_sensor_details()
        return self._form("edit_sensor", {vol.Required("sensor"): self._item_selector(sensors)}, {})

    async def async_step_edit_sensor_details(
        self, user_input: dict[str, Any] | None = None
    ) -> ConfigFlowResult:
        """Edit name and room; slave and pin only for a button."""
        assert self._target is not None
        sensor = self._target
        errors: dict[str, str] = {}
        is_button = sensor["type"] == SENSOR_TYPE_BUTTON
        if user_input is not None:
            slave = self._parse_slave(user_input, errors) if is_button else None
            if not errors:
                payload: dict[str, Any] = {"id": sensor["id"]}
                if user_input["name"] != sensor["name"]:
                    payload["name"] = user_input["name"]
                if int(user_input["room"]) != sensor["room"]:
                    payload["room"] = int(user_input["room"])
                if is_button and slave != sensor["slave"]:
                    payload["slave"] = slave
                if is_button and int(user_input["pin"]) != sensor.get("pin"):
                    payload["pin"] = int(user_input["pin"])
                if len(payload) == 1:
                    return self._done(sensor["name"])
                data = await self._request("sensor/update", payload, errors)
                if data is not None:
                    return self._done(user_input["name"])
        values = user_input or {
            "name": sensor["name"],
            "room": str(sensor["room"]),
            "slave": str(sensor["slave"]),
            "pin": sensor.get("pin"),
        }
        schema: dict[Any, Any] = {
            vol.Required("name", default=values["name"]): TextSelector(),
            vol.Required("room", default=values["room"]): self._room_selector(),
        }
        if is_button:
            schema[vol.Required("slave", default=values["slave"])] = self._slave_selector()
            schema[vol.Required("pin", default=values["pin"])] = PIN_SELECTOR
        return self._form("edit_sensor_details", schema, errors, {"name": sensor["name"]})

    async def async_step_remove_sensor(
        self, user_input: dict[str, Any] | None = None
    ) -> ConfigFlowResult:
        """Remove a sensor."""
        sensors = self._sensors()
        if not sensors:
            return self.async_abort(reason="no_sensors")
        errors: dict[str, str] = {}
        if user_input is not None:
            sensor = self._find(sensors, user_input["sensor"])
            if not user_input.get("confirm"):
                errors["confirm"] = "confirm_required"
            elif sensor is not None:
                data = await self._request("sensor/remove", {"id": sensor["id"]}, errors)
                if data is not None:
                    return self._done(sensor["name"])
        return self._form(
            "remove_sensor",
            {
                vol.Required(
                    "sensor", default=(user_input or {}).get("sensor", vol.UNDEFINED)
                ): self._item_selector(sensors),
                vol.Required("confirm", default=False): BooleanSelector(),
            },
            errors,
        )

    # ---------------------------------------------------------- click functions

    async def async_step_click_functions(
        self, user_input: dict[str, Any] | None = None
    ) -> ConfigFlowResult:
        """Pick the button whose local click functions are edited."""
        buttons = self._sensors(SENSOR_TYPE_BUTTON)
        if not buttons:
            return self.async_abort(reason="no_buttons")
        if user_input is not None:
            self._target = self._find(buttons, user_input["sensor"])
            if self._target is not None:
                return await self.async_step_click_functions_details()
        return self._form(
            "click_functions", {vol.Required("sensor"): self._item_selector(buttons)}, {}
        )

    async def async_step_click_functions_details(
        self, user_input: dict[str, Any] | None = None
    ) -> ConfigFlowResult:
        """For 1..N clicks: the device to control and the action.

        The function runs on the button's slave without the server, so only devices
        of the same slave can be controlled.
        """
        assert self._target is not None
        button = self._target
        errors: dict[str, str] = {}
        candidates = [dev for dev in self._devices() if dev["slave"] == button["slave"]]
        max_clicks = max(
            1, min(int(self._snapshot.get("max_click_functions", 3)), MAX_CLICK_FUNCTIONS)
        )
        if user_input is not None:
            functions = []
            for clicks in range(1, max_clicks + 1):
                device_id = user_input.get(f"device_{clicks}", NO_DEVICE)
                if device_id == NO_DEVICE:
                    continue
                device = self._find(candidates, device_id)
                action = user_input.get(f"action_{clicks}", ACTION_TOGGLE)
                # a blind is driven up/down/stop, everything else can only be toggled
                if device is None or (device["type"] == TYPE_BLIND) == (action == ACTION_TOGGLE):
                    errors[f"action_{clicks}"] = "invalid_action"
                    continue
                functions.append(
                    {"clicks": clicks, "device": device["id"], "state": CLICK_ACTIONS[action]}
                )
            if not errors:
                data = await self._request(
                    "sensor/update", {"id": button["id"], "click_functions": functions}, errors
                )
                if data is not None:
                    return self._done(button["name"])
        current = {fn["clicks"]: fn for fn in button.get("click_functions", [])}
        actions_by_state = {state: action for action, state in CLICK_ACTIONS.items()}
        device_options = [SelectOptionDict(value=NO_DEVICE, label="—")] + [
            SelectOptionDict(
                value=str(dev["id"]), label=f"{self._room_name(dev['room'])} / {dev['name']}"
            )
            for dev in candidates
        ]
        schema: dict[Any, Any] = {}
        for clicks in range(1, max_clicks + 1):
            function = current.get(clicks)
            default_device = NO_DEVICE
            default_action = ACTION_TOGGLE
            if function is not None and self._find(candidates, str(function["device"])):
                default_device = str(function["device"])
                default_action = actions_by_state.get(function["state"], ACTION_TOGGLE)
            if user_input is not None:
                default_device = user_input.get(f"device_{clicks}", default_device)
                default_action = user_input.get(f"action_{clicks}", default_action)
            schema[vol.Required(f"device_{clicks}", default=default_device)] = _select(device_options)
            schema[vol.Required(f"action_{clicks}", default=default_action)] = _select(
                list(CLICK_ACTIONS), "click_action"
            )
        return self._form(
            "click_functions_details",
            schema,
            errors,
            {"name": button["name"], "slave": str(button["slave"])},
        )

    # -------------------------------------------------------------------- rooms

    async def async_step_rooms(
        self, user_input: dict[str, Any] | None = None
    ) -> ConfigFlowResult:
        """Rooms submenu."""
        return self.async_show_menu(
            step_id="rooms", menu_options=["add_room", "rename_room", "remove_room", "init"]
        )

    async def async_step_add_room(
        self, user_input: dict[str, Any] | None = None
    ) -> ConfigFlowResult:
        """Add a room."""
        errors: dict[str, str] = {}
        if user_input is not None:
            data = await self._request("room/add", {"name": user_input["name"]}, errors)
            if data is not None:
                return self._done(user_input["name"])
        return self._form(
            "add_room",
            {vol.Required("name", default=(user_input or {}).get("name", vol.UNDEFINED)): TextSelector()},
            errors,
        )

    async def async_step_rename_room(
        self, user_input: dict[str, Any] | None = None
    ) -> ConfigFlowResult:
        """Rename a room, together with the Home Assistant area of the same name."""
        errors: dict[str, str] = {}
        if user_input is not None:
            room_id = int(user_input["room"])
            old_name = self._room_name(room_id)
            new_name = user_input["name"].strip()
            data = await self._request("room/rename", {"id": room_id, "name": new_name}, errors)
            if data is not None:
                # suggested_area only applies to new devices, so follow the rename ourselves
                areas = ar.async_get(self.hass)
                area = areas.async_get_area_by_name(old_name)
                if area is not None and areas.async_get_area_by_name(new_name) is None:
                    areas.async_update(area.id, name=new_name)
                return self._done(new_name)
        values = user_input or {}
        return self._form(
            "rename_room",
            {
                vol.Required("room", default=values.get("room", vol.UNDEFINED)): self._room_selector(),
                vol.Required("name", default=values.get("name", vol.UNDEFINED)): TextSelector(),
            },
            errors,
        )

    async def async_step_remove_room(
        self, user_input: dict[str, Any] | None = None
    ) -> ConfigFlowResult:
        """Remove an empty room."""
        errors: dict[str, str] = {}
        if user_input is not None:
            room_id = int(user_input["room"])
            name = self._room_name(room_id)
            data = await self._request("room/remove", {"id": room_id}, errors)
            if data is not None:
                return self._done(name)
        return self._form(
            "remove_room",
            {
                vol.Required(
                    "room", default=(user_input or {}).get("room", vol.UNDEFINED)
                ): self._room_selector()
            },
            errors,
        )

    # -------------------------------------------------------------- maintenance

    async def async_step_maintenance(
        self, user_input: dict[str, Any] | None = None
    ) -> ConfigFlowResult:
        """Maintenance submenu."""
        return self.async_show_menu(
            step_id="maintenance", menu_options=["restart_slaves", "reinit_slave", "init"]
        )

    async def async_step_restart_slaves(
        self, user_input: dict[str, Any] | None = None
    ) -> ConfigFlowResult:
        """Restart all slaves."""
        errors: dict[str, str] = {}
        if user_input is not None:
            if not user_input.get("confirm"):
                errors["confirm"] = "confirm_required"
            else:
                data = await self._request("bridge/restart_slaves", {}, errors)
                if data is not None:
                    return self._done("restart")
        return self._form(
            "restart_slaves", {vol.Required("confirm", default=False): BooleanSelector()}, errors
        )

    async def async_step_reinit_slave(
        self, user_input: dict[str, Any] | None = None
    ) -> ConfigFlowResult:
        """Re-send the whole configuration to one slave."""
        errors: dict[str, str] = {}
        if user_input is not None:
            slave = self._parse_slave(user_input, errors)
            if slave is not None:
                data = await self._request("bridge/reinit_slave", {"slave": slave}, errors)
                if data is not None:
                    return self._done(f"slave {slave}")
        return self._form(
            "reinit_slave",
            {
                vol.Required(
                    "slave", default=(user_input or {}).get("slave", vol.UNDEFINED)
                ): self._slave_selector()
            },
            errors,
        )
