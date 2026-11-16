"""Tests of the config flow and the management menu against a fake SmartHome server."""

import json
from typing import Any

import pytest
from pytest_homeassistant_custom_component.common import (
    MockConfigEntry,
    async_fire_mqtt_message,
)

from homeassistant.core import HomeAssistant
from homeassistant.data_entry_flow import FlowResultType
from homeassistant.helpers import area_registry as ar

from custom_components.smarthome_config.const import CONF_BASE_TOPIC, DOMAIN

SNAPSHOT = {
    "version": "2.0.1",
    "max_click_functions": 3,
    "slaves": [{"address": 18, "connected": True}, {"address": 19, "connected": False}],
    "rooms": [{"id": 0, "name": "Brak"}, {"id": 10, "name": "Salon"}],
    "devices": [
        {"id": 7, "name": "Lampa", "type": "LIGHT", "room": 10, "slave": 18, "pin": 5, "configured": True},
        {"id": 40, "name": "Taras", "type": "BLIND", "room": 10, "slave": 18, "pin": 16, "pin2": 15, "configured": True},
        {"id": 41, "name": "Daleka", "type": "LIGHT", "room": 0, "slave": 19, "pin": 3, "configured": False},
    ],
    "sensors": [
        {
            "id": 12,
            "name": "Włącznik",
            "type": "BUTTON",
            "room": 10,
            "slave": 18,
            "pin": 9,
            "click_functions": [{"clicks": 1, "device": 7, "state": "NONE"}],
        },
        {"id": 13, "name": "Termometr", "type": "THERMOMETR", "room": 0, "slave": 18},
    ],
}


class FakeServer:
    """Answers configuration requests the way the Java server does."""

    def __init__(self, hass: HomeAssistant, mqtt_mock) -> None:
        self.hass = hass
        self.requests: list[tuple[str, dict[str, Any]]] = []
        self.error: str | None = None
        self.silent = False
        mqtt_mock.async_publish.side_effect = self._on_publish

    def publish_snapshot(self) -> None:
        async_fire_mqtt_message(self.hass, "smarthome/bridge/config", json.dumps(SNAPSHOT))

    async def _on_publish(self, topic, payload, qos=0, retain=False) -> None:
        prefix = "smarthome/bridge/request/"
        if not topic.startswith(prefix) or self.silent:
            return
        action = topic[len(prefix) :]
        request = json.loads(payload)
        request_id = request.pop("request_id")
        self.requests.append((action, request))
        response: dict[str, Any] = {"request_id": request_id}
        if self.error:
            response.update(status="error", error=self.error)
        else:
            response.update(status="ok", data={"id": 99})
        async_fire_mqtt_message(
            self.hass, f"smarthome/bridge/response/{action}", json.dumps(response)
        )


@pytest.fixture
async def server(hass: HomeAssistant, mqtt_mock) -> FakeServer:
    return FakeServer(hass, mqtt_mock)


@pytest.fixture
async def flow_id(hass: HomeAssistant, server: FakeServer) -> str:
    """A loaded entry with the management menu open."""
    entry = MockConfigEntry(domain=DOMAIN, data={CONF_BASE_TOPIC: "smarthome"})
    entry.add_to_hass(hass)
    assert await hass.config_entries.async_setup(entry.entry_id)
    await hass.async_block_till_done()
    server.publish_snapshot()
    await hass.async_block_till_done()
    result = await hass.config_entries.options.async_init(entry.entry_id)
    assert result["type"] is FlowResultType.MENU
    return result["flow_id"]


async def _menu(hass: HomeAssistant, flow_id: str, step: str):
    return await hass.config_entries.options.async_configure(flow_id, {"next_step_id": step})


async def _submit(hass: HomeAssistant, flow_id: str, data: dict[str, Any]):
    return await hass.config_entries.options.async_configure(flow_id, data)


async def test_user_flow_requires_running_server(hass: HomeAssistant, server: FakeServer, monkeypatch) -> None:
    monkeypatch.setattr("custom_components.smarthome_config.config_flow.SNAPSHOT_TIMEOUT", 0.05)
    result = await hass.config_entries.flow.async_init(DOMAIN, context={"source": "user"})
    assert result["type"] is FlowResultType.FORM

    result = await hass.config_entries.flow.async_configure(result["flow_id"], {CONF_BASE_TOPIC: "smarthome"})
    assert result["errors"] == {"base": "no_server"}

    # the retained snapshot is delivered right after subscribing
    hass.loop.call_later(0.01, server.publish_snapshot)
    result = await hass.config_entries.flow.async_configure(result["flow_id"], {CONF_BASE_TOPIC: "smarthome/"})
    assert result["type"] is FlowResultType.CREATE_ENTRY
    assert result["data"] == {CONF_BASE_TOPIC: "smarthome"}


async def test_add_blind(hass: HomeAssistant, server: FakeServer, flow_id: str) -> None:
    result = await _menu(hass, flow_id, "add_device")
    result = await _submit(hass, flow_id, {"type": "BLIND"})
    assert result["step_id"] == "add_device_details"
    assert "pin2" in result["data_schema"].schema

    result = await _submit(hass, flow_id, {"name": "Roleta", "room": "10", "slave": "abc", "pin": 1, "pin2": 2})
    assert result["errors"] == {"slave": "invalid_slave"}
    assert not server.requests

    result = await _submit(hass, flow_id, {"name": "Roleta", "room": "10", "slave": "18", "pin": 1, "pin2": 2})
    assert result["type"] is FlowResultType.MENU
    assert result["step_id"] == "done"
    assert server.requests == [
        ("device/add", {"type": "BLIND", "name": "Roleta", "room": 10, "slave": 18, "pin": 1, "pin2": 2})
    ]

    result = await _menu(hass, flow_id, "finish")
    assert result["type"] is FlowResultType.CREATE_ENTRY


async def test_edit_device_sends_only_changes_and_shows_server_error(
    hass: HomeAssistant, server: FakeServer, flow_id: str
) -> None:
    await _menu(hass, flow_id, "edit_device")
    result = await _submit(hass, flow_id, {"device": "7"})
    assert result["step_id"] == "edit_device_details"
    assert "pin2" not in result["data_schema"].schema

    server.error = "Pin 9 na slave 18 jest już zajęty przez przycisk 'Włącznik'"
    result = await _submit(hass, flow_id, {"name": "Lampa", "room": "10", "slave": "18", "pin": 9})
    assert result["errors"] == {"base": "server_error"}
    assert result["description_placeholders"]["error"] == server.error

    server.error = None
    server.requests.clear()
    result = await _submit(hass, flow_id, {"name": "Żyrandol", "room": "0", "slave": "18", "pin": 5})
    assert result["step_id"] == "done"
    assert server.requests == [("device/update", {"id": 7, "name": "Żyrandol", "room": 0})]


async def test_unchanged_edit_sends_nothing(hass: HomeAssistant, server: FakeServer, flow_id: str) -> None:
    await _menu(hass, flow_id, "edit_sensor")
    result = await _submit(hass, flow_id, {"sensor": "13"})
    # a thermometer has no slave/pin to edit
    assert set(map(str, result["data_schema"].schema)) == {"name", "room"}
    result = await _submit(hass, flow_id, {"name": "Termometr", "room": "0"})
    assert result["step_id"] == "done"
    assert not server.requests


async def test_remove_device_needs_confirmation(hass: HomeAssistant, server: FakeServer, flow_id: str) -> None:
    await _menu(hass, flow_id, "remove_device")
    result = await _submit(hass, flow_id, {"device": "7", "confirm": False})
    assert result["errors"] == {"confirm": "confirm_required"}
    assert not server.requests
    result = await _submit(hass, flow_id, {"device": "7", "confirm": True})
    assert result["step_id"] == "done"
    assert server.requests == [("device/remove", {"id": 7})]


async def test_add_button_and_hygrometer(hass: HomeAssistant, server: FakeServer, flow_id: str) -> None:
    await _menu(hass, flow_id, "add_sensor")
    await _submit(hass, flow_id, {"type": "BUTTON"})
    await _submit(hass, flow_id, {"name": "Nowy", "room": "10", "slave": "18", "pin": 4})
    await _menu(hass, flow_id, "init")
    await _menu(hass, flow_id, "add_sensor")
    result = await _submit(hass, flow_id, {"type": "THERMOMETR_HYGROMETR"})
    assert "pin" not in result["data_schema"].schema
    await _submit(hass, flow_id, {"name": "Łazienka", "room": "10", "slave": "19"})
    assert server.requests == [
        ("sensor/add", {"type": "BUTTON", "name": "Nowy", "room": 10, "slave": 18, "pin": 4}),
        ("sensor/add", {"type": "THERMOMETR_HYGROMETR", "name": "Łazienka", "room": 10, "slave": 19}),
    ]


async def test_click_functions(hass: HomeAssistant, server: FakeServer, flow_id: str) -> None:
    await _menu(hass, flow_id, "click_functions")
    result = await _submit(hass, flow_id, {"sensor": "12"})
    assert result["step_id"] == "click_functions_details"
    schema = result["data_schema"].schema
    assert {str(key) for key in schema} == {f"{kind}_{n}" for kind in ("device", "action") for n in (1, 2, 3)}
    defaults = {str(key): key.default() for key in schema}
    assert defaults["device_1"] == "7" and defaults["action_1"] == "toggle"
    assert defaults["device_2"] == "none"
    # only devices of the button's slave can be controlled locally
    device_values = [option["value"] for option in schema["device_1"].config["options"]]
    assert device_values == ["none", "7", "40"]

    # a blind cannot be toggled, a light cannot be driven up
    result = await _submit(
        hass,
        flow_id,
        {
            "device_1": "40", "action_1": "toggle",
            "device_2": "7", "action_2": "up",
            "device_3": "none", "action_3": "toggle",
        },
    )
    assert result["errors"] == {"action_1": "invalid_action", "action_2": "invalid_action"}
    assert not server.requests

    result = await _submit(
        hass,
        flow_id,
        {
            "device_1": "7", "action_1": "toggle",
            "device_2": "40", "action_2": "down",
            "device_3": "none", "action_3": "stop",
        },
    )
    assert result["step_id"] == "done"
    assert server.requests == [
        (
            "sensor/update",
            {
                "id": 12,
                "click_functions": [
                    {"clicks": 1, "device": 7, "state": "NONE"},
                    {"clicks": 2, "device": 40, "state": "DOWN"},
                ],
            },
        )
    ]


async def test_rename_room_renames_matching_area(hass: HomeAssistant, server: FakeServer, flow_id: str) -> None:
    areas = ar.async_get(hass)
    area = areas.async_create("Salon")

    await _menu(hass, flow_id, "rooms")
    await _menu(hass, flow_id, "rename_room")
    result = await _submit(hass, flow_id, {"room": "10", "name": "Pokój dzienny"})

    assert result["step_id"] == "done"
    assert server.requests == [("room/rename", {"id": 10, "name": "Pokój dzienny"})]
    assert areas.async_get_area(area.id).name == "Pokój dzienny"


async def test_rooms_and_maintenance(hass: HomeAssistant, server: FakeServer, flow_id: str) -> None:
    await _menu(hass, flow_id, "rooms")
    await _menu(hass, flow_id, "add_room")
    await _submit(hass, flow_id, {"name": "Kuchnia"})
    await _menu(hass, flow_id, "init")
    await _menu(hass, flow_id, "rooms")
    await _menu(hass, flow_id, "remove_room")
    await _submit(hass, flow_id, {"room": "10"})
    await _menu(hass, flow_id, "init")
    await _menu(hass, flow_id, "maintenance")
    await _menu(hass, flow_id, "reinit_slave")
    await _submit(hass, flow_id, {"slave": "18"})
    await _menu(hass, flow_id, "init")
    await _menu(hass, flow_id, "maintenance")
    await _menu(hass, flow_id, "restart_slaves")
    result = await _submit(hass, flow_id, {"confirm": True})
    assert result["step_id"] == "done"
    assert server.requests == [
        ("room/add", {"name": "Kuchnia"}),
        ("room/remove", {"id": 10}),
        ("bridge/reinit_slave", {"slave": 18}),
        ("bridge/restart_slaves", {}),
    ]


async def test_timeout_is_reported(hass: HomeAssistant, server: FakeServer, flow_id: str, mqtt_mock, monkeypatch) -> None:
    monkeypatch.setattr("custom_components.smarthome_config.client.REQUEST_TIMEOUT", 0.05)
    server.silent = True
    await _menu(hass, flow_id, "rooms")
    await _menu(hass, flow_id, "add_room")
    result = await _submit(hass, flow_id, {"name": "Kuchnia"})
    assert result["errors"] == {"base": "timeout"}
