"""SmartHome Config: manage the SmartHome server (rooms, devices, sensors) from Home Assistant.

The entities themselves come from the server's MQTT discovery; this integration only adds
the configuration UI (the "Configure" button of the integration) on top of the server's
MQTT configuration API.
"""

from __future__ import annotations

from homeassistant.components import mqtt
from homeassistant.config_entries import ConfigEntry
from homeassistant.core import HomeAssistant
from homeassistant.exceptions import ConfigEntryNotReady

from .client import SmartHomeClient
from .const import CONF_BASE_TOPIC, DEFAULT_BASE_TOPIC

type SmartHomeConfigEntry = ConfigEntry[SmartHomeClient]


async def async_setup_entry(hass: HomeAssistant, entry: SmartHomeConfigEntry) -> bool:
    """Set up the MQTT client of the configuration API."""
    if not await mqtt.async_wait_for_mqtt_client(hass):
        raise ConfigEntryNotReady("MQTT integration is not available")
    client = SmartHomeClient(hass, entry.data.get(CONF_BASE_TOPIC, DEFAULT_BASE_TOPIC))
    await client.async_start()
    entry.runtime_data = client
    return True


async def async_unload_entry(hass: HomeAssistant, entry: SmartHomeConfigEntry) -> bool:
    """Stop the MQTT client."""
    await entry.runtime_data.async_stop()
    return True
