"""MQTT client of the SmartHome server configuration API.

Requests go to ``<base>/bridge/request/<action>`` and are answered on
``<base>/bridge/response/<action>`` with the ``request_id`` echoed back. The server also
keeps a retained snapshot of its configuration on ``<base>/bridge/config``.
"""

from __future__ import annotations

import asyncio
from collections.abc import Callable
import json
import logging
from typing import Any
import uuid

from homeassistant.components import mqtt
from homeassistant.core import HomeAssistant, callback
from homeassistant.exceptions import HomeAssistantError

from .const import REQUEST_TIMEOUT

_LOGGER = logging.getLogger(__name__)


class SmartHomeError(HomeAssistantError):
    """The server rejected a request or did not answer."""


class SmartHomeClient:
    """Talks to the SmartHome server over MQTT."""

    def __init__(self, hass: HomeAssistant, base_topic: str) -> None:
        self._hass = hass
        self._base = base_topic.strip().strip("/")
        self._unsubscribe: list[Callable[[], None]] = []
        self._pending: dict[str, asyncio.Future[dict[str, Any]]] = {}
        self._snapshot_event = asyncio.Event()
        self.snapshot: dict[str, Any] | None = None

    async def async_start(self) -> None:
        """Subscribe to the snapshot and to responses."""
        self._unsubscribe.append(
            await mqtt.async_subscribe(
                self._hass, f"{self._base}/bridge/config", self._on_snapshot, qos=1
            )
        )
        self._unsubscribe.append(
            await mqtt.async_subscribe(
                self._hass, f"{self._base}/bridge/response/#", self._on_response, qos=1
            )
        )

    async def async_stop(self) -> None:
        """Unsubscribe and fail requests still waiting for an answer."""
        for unsubscribe in self._unsubscribe:
            unsubscribe()
        self._unsubscribe.clear()
        for future in self._pending.values():
            if not future.done():
                future.cancel()
        self._pending.clear()

    @callback
    def _on_snapshot(self, msg: mqtt.ReceiveMessage) -> None:
        try:
            snapshot = json.loads(msg.payload)
        except (TypeError, ValueError):
            _LOGGER.warning("Invalid configuration snapshot on %s", msg.topic)
            return
        if isinstance(snapshot, dict):
            self.snapshot = snapshot
            self._snapshot_event.set()

    @callback
    def _on_response(self, msg: mqtt.ReceiveMessage) -> None:
        try:
            response = json.loads(msg.payload)
        except (TypeError, ValueError):
            return
        if not isinstance(response, dict):
            return
        future = self._pending.get(str(response.get("request_id")))
        if future is not None and not future.done():
            future.set_result(response)

    async def async_wait_for_snapshot(self, timeout: float) -> bool:
        """Wait until the server's snapshot arrives; False when it did not."""
        try:
            async with asyncio.timeout(timeout):
                await self._snapshot_event.wait()
        except TimeoutError:
            return False
        return True

    async def async_request(
        self, action: str, payload: dict[str, Any] | None = None
    ) -> dict[str, Any]:
        """Send a request and return the ``data`` of its response.

        Raises SmartHomeError with the server's message when the request was rejected.
        """
        request_id = uuid.uuid4().hex
        future: asyncio.Future[dict[str, Any]] = self._hass.loop.create_future()
        self._pending[request_id] = future
        try:
            await mqtt.async_publish(
                self._hass,
                f"{self._base}/bridge/request/{action}",
                json.dumps({**(payload or {}), "request_id": request_id}),
                qos=1,
                retain=False,
            )
            async with asyncio.timeout(REQUEST_TIMEOUT):
                response = await future
        except TimeoutError as err:
            raise SmartHomeError("timeout") from err
        except HomeAssistantError as err:
            raise SmartHomeError(str(err)) from err
        finally:
            self._pending.pop(request_id, None)
        if response.get("status") != "ok":
            raise SmartHomeError(str(response.get("error") or "unknown error"))
        data = response.get("data")
        return data if isinstance(data, dict) else {}
