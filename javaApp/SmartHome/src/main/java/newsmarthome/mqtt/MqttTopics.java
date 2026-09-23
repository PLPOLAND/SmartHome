package newsmarthome.mqtt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import newsmarthome.model.hardware.device.Blind;
import newsmarthome.model.hardware.device.Device;
import newsmarthome.model.hardware.device.DeviceState;
import newsmarthome.model.hardware.device.DeviceTypes;
import newsmarthome.model.hardware.sensor.Higrometr;
import newsmarthome.model.hardware.sensor.Sensor;
import newsmarthome.model.hardware.sensor.SensorsTypes;
import newsmarthome.model.hardware.sensor.Termometr;

/**
 * Czyste funkcje budujące nazwy topiców MQTT oraz payloady Home Assistant MQTT Discovery.
 * Nie mają zależności od I2C/hardware, więc są łatwe do przetestowania jednostkowo.
 */
public final class MqttTopics {

    private static final String SENSOR_COMPONENT = "sensor";
    private static final int BLIND_POSITION_OPEN = 100;
    private static final int BLIND_POSITION_CLOSED = 0;
    /** Umowna pozycja rolety zatrzymanej w połowie - sprzęt nie mierzy rzeczywistej pozycji. */
    private static final int BLIND_POSITION_STOPPED = 50;

    private MqttTopics() {
    }

    public static String availabilityTopic(String baseTopic) {
        return baseTopic + "/bridge/status";
    }

    public static String deviceObjectId(int deviceId) {
        return "smarthome_device_" + deviceId;
    }

    public static String sensorObjectId(int sensorId) {
        return "smarthome_sensor_" + sensorId;
    }

    public static String humidityObjectId(int sensorId) {
        return sensorObjectId(sensorId) + "_humidity";
    }

    public static String deviceStateTopic(String baseTopic, int deviceId) {
        return baseTopic + "/device/" + deviceId + "/state";
    }

    public static String devicePositionTopic(String baseTopic, int deviceId) {
        return baseTopic + "/device/" + deviceId + "/position";
    }

    public static String deviceCommandTopic(String baseTopic, int deviceId) {
        return baseTopic + "/device/" + deviceId + "/set";
    }

    public static String deviceCommandTopicFilter(String baseTopic) {
        return baseTopic + "/device/+/set";
    }

    public static String sensorStateTopic(String baseTopic, int sensorId) {
        return baseTopic + "/sensor/" + sensorId + "/state";
    }

    public static String discoveryConfigTopic(String discoveryPrefix, String component, String objectId) {
        return discoveryPrefix + "/" + component + "/" + objectId + "/config";
    }

    /**
     * Zwraca komponent Home Assistant odpowiadający danemu typowi urządzenia, lub null
     * jeśli typ nie jest obsługiwany przez integrację MQTT.
     */
    public static String haComponentForDevice(DeviceTypes typ) {
        switch (typ) {
            case LIGHT:
                return "light";
            case GNIAZDKO:
                return "switch";
            case WENTYLATOR:
                return "fan";
            case BLIND:
                return "cover";
            default:
                return null;
        }
    }

    /**
     * Wyciąga id urządzenia z topicu komendy zbudowanego przez {@link #deviceCommandTopic}.
     * @return id urządzenia lub null jeśli topic nie pasuje do wzorca.
     */
    public static Integer parseDeviceIdFromCommandTopic(String baseTopic, String topic) {
        String prefix = baseTopic + "/device/";
        String suffix = "/set";
        if (topic == null || !topic.startsWith(prefix) || !topic.endsWith(suffix)) {
            return null;
        }
        String idPart = topic.substring(prefix.length(), topic.length() - suffix.length());
        try {
            return Integer.parseInt(idPart);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Map<String, Object> baseAvailability(String baseTopic) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("availability_topic", availabilityTopic(baseTopic));
        map.put("payload_available", "online");
        map.put("payload_not_available", "offline");
        return map;
    }

    private static Map<String, Object> deviceInfo(String identifier, String name, String model, String area) {
        Map<String, Object> device = new LinkedHashMap<>();
        device.put("identifiers", Collections.singletonList(identifier));
        device.put("name", name);
        device.put("manufacturer", "SmartHome");
        device.put("model", model);
        // HA przypisuje obszar tylko przy tworzeniu urządzenia w rejestrze
        if (area != null && !area.isEmpty()) {
            device.put("suggested_area", area);
        }
        return device;
    }

    /**
     * Buduje payload discovery dla urządzenia. Zwraca null dla nieobsługiwanych typów.
     * @param roomName nazwa pokoju przekazywana do HA jako obszar (może być null)
     */
    public static Map<String, Object> deviceDiscoveryConfig(Device device, String baseTopic, String roomName) {
        String component = haComponentForDevice(device.getTyp());
        if (component == null) {
            return null;
        }
        String objectId = deviceObjectId(device.getId());
        Map<String, Object> config = baseAvailability(baseTopic);
        config.put("name", device.getName());
        config.put("unique_id", objectId);
        config.put("state_topic", deviceStateTopic(baseTopic, device.getId()));
        config.put("command_topic", deviceCommandTopic(baseTopic, device.getId()));
        config.put("device", deviceInfo(objectId, device.getName(), component, roomName));

        switch (device.getTyp()) {
            case LIGHT:
                config.put("payload_on", DeviceState.ON.name());
                config.put("payload_off", DeviceState.OFF.name());
                break;
            case GNIAZDKO:
                config.put("payload_on", DeviceState.ON.name());
                config.put("payload_off", DeviceState.OFF.name());
                config.put("device_class", "outlet");
                break;
            case WENTYLATOR:
                config.put("payload_on", DeviceState.ON.name());
                config.put("payload_off", DeviceState.OFF.name());
                break;
            case BLIND:
                config.put("payload_open", "OPEN");
                config.put("payload_close", "CLOSE");
                // STOP zatrzymuje roletę w ruchu (komenda 'S' do slave'a)
                config.put("payload_stop", "STOP");
                config.put("state_open", "open");
                config.put("state_closed", "closed");
                config.put("state_opening", "opening");
                config.put("state_closing", "closing");
                // bez pozycji HA zamienia "stopped" po "closing" na "closed", a "open" traktuje
                // jako w pełni otwartą (blokuje przycisk w górę) - stąd umowna pozycja
                config.put("position_topic", devicePositionTopic(baseTopic, device.getId()));
                config.put("position_open", BLIND_POSITION_OPEN);
                config.put("position_closed", BLIND_POSITION_CLOSED);
                break;
            default:
                return null;
        }
        return config;
    }

    /**
     * Mapuje aktualny stan urządzenia na payload publikowany na state_topic. Zwraca null
     * jeśli stanu nie da się jednoznacznie zmapować (np. NOTKNOW rolety).
     */
    public static String deviceStatePayload(Device device) {
        DeviceState state = device.getState();
        if (state == null) {
            return null;
        }
        if (device.getTyp() == DeviceTypes.BLIND) {
            Blind blind = (Blind) device;
            switch (state) {
                case UP:
                    return "open";
                case DOWN:
                    return "closed";
                case RUN:
                    if (blind.getSwitchUp().getStan() == DeviceState.ON) {
                        return "opening";
                    }
                    if (blind.getSwitchDown().getStan() == DeviceState.ON) {
                        return "closing";
                    }
                    return null;
                case NOTKNOW:
                    // zatrzymana w połowie - "open" z pozycją pośrednią (patrz blindPositionPayload)
                    return "open";
                default:
                    return null;
            }
        }
        if (state == DeviceState.ON || state == DeviceState.OFF) {
            return state.name();
        }
        return null;
    }

    /**
     * Mapuje stan rolety na payload publikowany na position_topic. Zwraca null dla urządzeń
     * innych niż roleta oraz w trakcie ruchu (zostaje ostatnio opublikowana pozycja).
     */
    public static String blindPositionPayload(Device device) {
        if (device.getTyp() != DeviceTypes.BLIND || device.getState() == null) {
            return null;
        }
        switch (device.getState()) {
            case UP:
                return String.valueOf(BLIND_POSITION_OPEN);
            case DOWN:
                return String.valueOf(BLIND_POSITION_CLOSED);
            case NOTKNOW:
                return String.valueOf(BLIND_POSITION_STOPPED);
            default:
                return null;
        }
    }

    /**
     * Mapuje payload komendy HA na docelowy DeviceState urządzenia. Zwraca null jeśli
     * payload jest nieznany dla danego typu urządzenia.
     */
    public static DeviceState commandPayloadToState(DeviceTypes typ, String payload) {
        if (payload == null) {
            return null;
        }
        String normalized = payload.trim().toUpperCase();
        if (typ == DeviceTypes.BLIND) {
            if ("OPEN".equals(normalized) || "UP".equals(normalized)) {
                return DeviceState.UP;
            }
            if ("CLOSE".equals(normalized) || "DOWN".equals(normalized)) {
                return DeviceState.DOWN;
            }
            if ("STOP".equals(normalized)) {
                return DeviceState.NOTKNOW;
            }
            return null;
        }
        if ("ON".equals(normalized)) {
            return DeviceState.ON;
        }
        if ("OFF".equals(normalized)) {
            return DeviceState.OFF;
        }
        return null;
    }

    /**
     * Buduje configi discovery dla czujnika (temperatura, opcjonalnie wilgotność). Zwraca
     * pustą listę dla typów sensorów jeszcze nieobsługiwanych przez integrację MQTT.
     * @param roomName nazwa pokoju przekazywana do HA jako obszar (może być null)
     */
    public static List<Map<String, Object>> sensorDiscoveryConfigs(Sensor sensor, String baseTopic, String roomName) {
        List<Map<String, Object>> configs = new ArrayList<>();
        if (sensor.getTyp() != SensorsTypes.THERMOMETR && sensor.getTyp() != SensorsTypes.THERMOMETR_HYGROMETR) {
            return configs;
        }
        String stateTopic = sensorStateTopic(baseTopic, sensor.getId());
        String tempObjectId = sensorObjectId(sensor.getId());

        Map<String, Object> temperature = baseAvailability(baseTopic);
        temperature.put("name", sensor.getNazwa());
        temperature.put("unique_id", tempObjectId);
        temperature.put("state_topic", stateTopic);
        temperature.put("unit_of_measurement", "°C");
        temperature.put("device_class", "temperature");
        temperature.put("state_class", "measurement");
        temperature.put("value_template", "{{ value_json.temperature }}");
        temperature.put("device", deviceInfo(tempObjectId, sensor.getNazwa(), SENSOR_COMPONENT, roomName));
        configs.add(temperature);

        if (sensor.getTyp() == SensorsTypes.THERMOMETR_HYGROMETR) {
            Map<String, Object> humidity = baseAvailability(baseTopic);
            humidity.put("name", sensor.getNazwa() + " Wilgotność");
            humidity.put("unique_id", humidityObjectId(sensor.getId()));
            humidity.put("state_topic", stateTopic);
            humidity.put("unit_of_measurement", "%");
            humidity.put("device_class", "humidity");
            humidity.put("state_class", "measurement");
            humidity.put("value_template", "{{ value_json.humidity }}");
            humidity.put("device", deviceInfo(tempObjectId, sensor.getNazwa(), SENSOR_COMPONENT, roomName));
            configs.add(humidity);
        }
        return configs;
    }

    /**
     * Buduje payload (jako Map, do serializacji JSON) publikowany na state_topic czujnika.
     * Zwraca null, dopóki czujnik nie ma prawdziwego odczytu (modele inicjalizują wartości
     * placeholderem MAX_VALUE, który w HA trafiłby do statystyk jako realny pomiar).
     */
    public static Map<String, Object> sensorStatePayload(Sensor sensor) {
        Map<String, Object> payload = new LinkedHashMap<>();
        if (sensor.getTyp() == SensorsTypes.THERMOMETR) {
            Float temperature = ((Termometr) sensor).getTemperatura();
            if (temperature == null || temperature == Float.MAX_VALUE) {
                return null;
            }
            payload.put("temperature", temperature);
        } else if (sensor.getTyp() == SensorsTypes.THERMOMETR_HYGROMETR) {
            Higrometr higrometr = (Higrometr) sensor;
            Float temperature = higrometr.getTemperatura();
            Integer humidity = higrometr.getHumidity();
            if (temperature == null || temperature == Float.MAX_VALUE || humidity == null
                    || humidity == Integer.MAX_VALUE) {
                return null;
            }
            payload.put("temperature", temperature);
            payload.put("humidity", humidity);
        }
        return payload;
    }
}
