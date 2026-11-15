package newsmarthome.mqtt;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import javax.annotation.PreDestroy;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import newsmarthome.model.hardware.device.DeviceTypes;
import newsmarthome.model.hardware.sensor.ButtonLocalFunction;
import newsmarthome.model.hardware.sensor.SensorsTypes;
import newsmarthome.service.ClickFunctionSpec;
import newsmarthome.service.ConfigurationException;
import newsmarthome.service.ConfigurationService;

/**
 * API konfiguracji po MQTT dla integracji Home Assistant. Żądanie JSON na
 * {@code <base>/bridge/request/<akcja>}, odpowiedź na {@code <base>/bridge/response/<akcja>}:
 * {@code {"id": <id z żądania>, "status": "ok"|"error", "error": "...", "data": {...}}}.
 * Akcje: room/add|rename|remove, device/add|update|remove, sensor/add|update|remove,
 * bridge/restart_slaves, bridge/reinit_slave.
 */
@Service
public class MqttConfigHandler {

    private final Logger logger = LoggerFactory.getLogger(MqttConfigHandler.class);
    private final MqttGateway gateway;
    private final ConfigurationService configurationService;
    private final BridgeConfigPublisher bridgeConfigPublisher;
    private final ObjectMapper objectMapper = new ObjectMapper();
    // zmiany konfiguracji robią synchroniczne I/O na I2C - nie mogą blokować wątku callbacków Paho;
    // jeden wątek zachowuje kolejność żądań
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "mqtt-config");
        t.setDaemon(true);
        return t;
    });

    public MqttConfigHandler(MqttGateway gateway, ConfigurationService configurationService,
            BridgeConfigPublisher bridgeConfigPublisher) {
        this.gateway = gateway;
        this.configurationService = configurationService;
        this.bridgeConfigPublisher = bridgeConfigPublisher;
    }

    @PreDestroy
    public void stop() {
        executor.shutdown();
    }

    public void subscribe() {
        gateway.subscribe(MqttTopics.bridgeRequestTopicFilter(gateway.getBaseTopic()), this::handleMessage);
    }

    private void handleMessage(String topic, MqttMessage message) {
        String action = MqttTopics.parseBridgeAction(gateway.getBaseTopic(), topic);
        if (action == null || message.isRetained()) {
            // retained żądanie wykonywałoby się ponownie po każdym połączeniu
            logger.warn("Pominięto żądanie konfiguracji na topicu {}", topic);
            return;
        }
        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
        try {
            executor.execute(() -> gateway.publish(MqttTopics.bridgeResponseTopic(gateway.getBaseTopic(), action),
                    handle(action, payload), false));
        } catch (RejectedExecutionException e) {
            logger.warn("Pominięto żądanie konfiguracji {} - handler jest zatrzymany", action);
        }
    }

    /** Wykonuje żądanie i zwraca odpowiedź JSON. */
    String handle(String action, String payload) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            JsonNode request = payload == null || payload.trim().isEmpty() ? objectMapper.createObjectNode()
                    : objectMapper.readTree(payload);
            if (request == null || !request.isObject()) {
                throw new ConfigurationException("Żądanie musi być obiektem JSON");
            }
            if (request.has("id")) {
                response.put("id", request.get("id"));
            }
            logger.info("Żądanie konfiguracji z MQTT: {} {}", action, payload);
            Object data = execute(action, request);
            response.put("status", "ok");
            if (data != null) {
                response.put("data", data);
            }
        } catch (ConfigurationException e) {
            response.put("status", "error");
            response.put("error", e.getMessage());
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            response.put("status", "error");
            response.put("error", "Niepoprawny JSON żądania");
        } catch (RuntimeException e) {
            logger.error("Błąd podczas wykonywania żądania konfiguracji {}: {}", action, e.getMessage(), e);
            response.put("status", "error");
            response.put("error", "Błąd serwera: " + e.getMessage());
        }
        try {
            return objectMapper.writeValueAsString(response);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            return "{\"status\":\"error\",\"error\":\"Błąd serializacji odpowiedzi\"}";
        }
    }

    private Object execute(String action, JsonNode request) {
        switch (action) {
            case "room/add":
                return MqttTopics.roomConfigPayload(configurationService.addRoom(text(request, "name")));
            case "room/rename":
                return MqttTopics.roomConfigPayload(
                        configurationService.renameRoom(integer(request, "id"), text(request, "name")));
            case "room/remove":
                configurationService.removeRoom(integer(request, "id"));
                return null;
            case "device/add":
                return MqttTopics.deviceConfigPayload(configurationService.addDevice(text(request, "name"),
                        DeviceTypes.fromString(text(request, "type")), integer(request, "room"),
                        integer(request, "slave"), optInteger(request, "pin"), optInteger(request, "pin2")));
            case "device/update":
                return MqttTopics.deviceConfigPayload(configurationService.updateDevice(integer(request, "id"),
                        optText(request, "name"), optInteger(request, "room"), optInteger(request, "slave"),
                        optInteger(request, "pin"), optInteger(request, "pin2")));
            case "device/remove":
                configurationService.removeDevice(integer(request, "id"));
                return null;
            case "sensor/add":
                return MqttTopics.sensorConfigPayload(configurationService.addSensor(
                        SensorsTypes.fromString(text(request, "type")), text(request, "name"),
                        integer(request, "room"), integer(request, "slave"), optInteger(request, "pin"),
                        clickFunctions(request)));
            case "sensor/update":
                return MqttTopics.sensorConfigPayload(configurationService.updateSensor(integer(request, "id"),
                        optText(request, "name"), optInteger(request, "room"), optInteger(request, "slave"),
                        optInteger(request, "pin"), clickFunctions(request)));
            case "sensor/remove":
                configurationService.removeSensor(integer(request, "id"));
                return null;
            case "bridge/restart_slaves":
                configurationService.restartSlaves();
                return null;
            case "bridge/reinit_slave":
                configurationService.reinitSlave(integer(request, "slave"));
                return null;
            default:
                throw new ConfigurationException("Nieznana akcja: " + action);
        }
    }

    private static String text(JsonNode request, String field) {
        String value = optText(request, field);
        if (value == null) {
            throw new ConfigurationException("Brak pola '" + field + "'");
        }
        return value;
    }

    private static String optText(JsonNode request, String field) {
        JsonNode node = request.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isTextual()) {
            throw new ConfigurationException("Pole '" + field + "' musi być tekstem");
        }
        return node.asText();
    }

    private static int integer(JsonNode request, String field) {
        Integer value = optInteger(request, field);
        if (value == null) {
            throw new ConfigurationException("Brak pola '" + field + "'");
        }
        return value;
    }

    private static Integer optInteger(JsonNode request, String field) {
        JsonNode node = request.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isInt()) {
            throw new ConfigurationException("Pole '" + field + "' musi być liczbą całkowitą");
        }
        return node.asInt();
    }

    /** @return null, gdy żądanie nie zawiera click_functions (bez zmian) */
    private List<ClickFunctionSpec> clickFunctions(JsonNode request) {
        JsonNode list = request.get("click_functions");
        if (list == null || list.isNull()) {
            return null;
        }
        if (!list.isArray()) {
            throw new ConfigurationException("Pole 'click_functions' musi być listą");
        }
        List<ClickFunctionSpec> functions = new ArrayList<>();
        for (JsonNode node : list) {
            if (!node.isObject()) {
                throw new ConfigurationException("Funkcja kliknięcia musi być obiektem");
            }
            int clicks = integer(node, "clicks");
            if (clicks > bridgeConfigPublisher.getMaxClickFunctions()) {
                throw new ConfigurationException(
                        "Obsługiwane są funkcje dla 1.." + bridgeConfigPublisher.getMaxClickFunctions() + " kliknięć");
            }
            String state = text(node, "state").toUpperCase();
            ButtonLocalFunction.State parsed;
            try {
                parsed = ButtonLocalFunction.State.valueOf(state);
            } catch (IllegalArgumentException e) {
                throw new ConfigurationException("Nieznana akcja funkcji kliknięcia: " + state);
            }
            functions.add(new ClickFunctionSpec(clicks, integer(node, "device"), parsed));
        }
        return functions;
    }
}
