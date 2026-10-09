package newsmarthome.mqtt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import newsmarthome.model.hardware.device.Blind;
import newsmarthome.model.hardware.device.DeviceState;
import newsmarthome.model.hardware.device.DeviceTypes;
import newsmarthome.model.hardware.device.Light;
import newsmarthome.model.hardware.sensor.Button;
import newsmarthome.model.hardware.sensor.ButtonClickType;
import newsmarthome.model.hardware.sensor.Higrometr;
import newsmarthome.model.hardware.sensor.SensorsTypes;
import newsmarthome.model.hardware.sensor.Termometr;

class MqttTopicsTest {

    @Test
    void buildsDeviceTopics() {
        assertEquals("smarthome/device/5/state", MqttTopics.deviceStateTopic("smarthome", 5));
        assertEquals("smarthome/device/5/set", MqttTopics.deviceCommandTopic("smarthome", 5));
        assertEquals("smarthome/device/+/set", MqttTopics.deviceCommandTopicFilter("smarthome"));
        assertEquals(Integer.valueOf(5), MqttTopics.parseDeviceIdFromCommandTopic("smarthome", "smarthome/device/5/set"));
        assertNull(MqttTopics.parseDeviceIdFromCommandTopic("smarthome", "smarthome/device/abc/set"));
        assertNull(MqttTopics.parseDeviceIdFromCommandTopic("smarthome", "other/device/5/set"));
    }

    @Test
    void mapsHaComponentPerDeviceType() {
        assertEquals("light", MqttTopics.haComponentForDevice(DeviceTypes.LIGHT));
        assertEquals("switch", MqttTopics.haComponentForDevice(DeviceTypes.GNIAZDKO));
        assertEquals("fan", MqttTopics.haComponentForDevice(DeviceTypes.WENTYLATOR));
        assertEquals("cover", MqttTopics.haComponentForDevice(DeviceTypes.BLIND));
        assertNull(MqttTopics.haComponentForDevice(DeviceTypes.NONE));
    }

    @Test
    void buildsLightDiscoveryConfig() {
        Light light = new Light(13);
        light.setId(7);
        light.setName("Poziom 1");

        Map<String, Object> config = MqttTopics.deviceDiscoveryConfig(light, "smarthome", "Salon");

        assertEquals("Poziom 1", config.get("name"));
        assertEquals("smarthome_device_7", config.get("unique_id"));
        assertEquals("smarthome/device/7/state", config.get("state_topic"));
        assertEquals("smarthome/device/7/set", config.get("command_topic"));
        assertEquals("ON", config.get("payload_on"));
        assertEquals("OFF", config.get("payload_off"));
        assertEquals("Salon", ((Map<?, ?>) config.get("device")).get("suggested_area"));
    }

    @Test
    void omitsSuggestedAreaWhenRoomUnknown() {
        Light light = new Light(13);
        light.setId(8);
        Map<?, ?> device = (Map<?, ?>) MqttTopics.deviceDiscoveryConfig(light, "smarthome", null).get("device");
        assertTrue(!device.containsKey("suggested_area"));
    }

    @Test
    void mapsBlindStateIncludingMovement() {
        Blind blind = new Blind();
        blind.setId(9);

        Map<String, Object> config = MqttTopics.deviceDiscoveryConfig(blind, "smarthome", null);
        assertEquals("STOP", config.get("payload_stop"));
        assertEquals("smarthome/device/9/position", config.get("position_topic"));
        assertTrue(!config.containsKey("state_stopped"));

        // nieznany stan: brak publikacji i brak efektu ubocznego (getState() ustawiłby NOTKNOW)
        assertNull(MqttTopics.deviceStatePayload(blind));
        assertNull(MqttTopics.blindPositionPayload(blind));
        assertNull(blind.peekState());

        blind.changeState(DeviceState.UP);
        assertEquals("open", MqttTopics.deviceStatePayload(blind));
        assertEquals("100", MqttTopics.blindPositionPayload(blind));

        blind.changeState(DeviceState.DOWN);
        assertEquals("closed", MqttTopics.deviceStatePayload(blind));
        assertEquals("0", MqttTopics.blindPositionPayload(blind));

        // symuluje odczyt 'R' (w ruchu) z hardware, gdy ostatnia komenda to DOWN
        blind.changeState(DeviceState.RUN);
        assertEquals("closing", MqttTopics.deviceStatePayload(blind));
        assertNull(MqttTopics.blindPositionPayload(blind));

        blind.changeState(DeviceState.UP);
        blind.changeState(DeviceState.RUN);
        assertEquals("opening", MqttTopics.deviceStatePayload(blind));

        blind.changeState(DeviceState.NOTKNOW);
        // zatrzymana w trakcie zamykania: "open" z pozycją pośrednią, nie "closed"
        assertEquals("open", MqttTopics.deviceStatePayload(blind));
        assertEquals("50", MqttTopics.blindPositionPayload(blind));
    }

    @Test
    void mapsHaCommandPayloadsToDeviceState() {
        assertEquals(DeviceState.ON, MqttTopics.commandPayloadToState(DeviceTypes.LIGHT, "on"));
        assertEquals(DeviceState.OFF, MqttTopics.commandPayloadToState(DeviceTypes.LIGHT, "OFF"));
        assertNull(MqttTopics.commandPayloadToState(DeviceTypes.LIGHT, "OPEN"));

        assertEquals(DeviceState.UP, MqttTopics.commandPayloadToState(DeviceTypes.BLIND, "OPEN"));
        assertEquals(DeviceState.DOWN, MqttTopics.commandPayloadToState(DeviceTypes.BLIND, "CLOSE"));
        assertEquals(DeviceState.NOTKNOW, MqttTopics.commandPayloadToState(DeviceTypes.BLIND, "STOP"));
        assertNull(MqttTopics.commandPayloadToState(DeviceTypes.BLIND, "ON"));
    }

    @Test
    void buildsThermometerDiscoveryAndState() {
        Termometr termometr = new Termometr();
        termometr.setId(100);
        termometr.setNazwa("Kotłownia");
        termometr.setTemperatura(22.5f);

        List<Map<String, Object>> configs = MqttTopics.sensorDiscoveryConfigs(termometr, "smarthome", "Kotłownia");
        assertEquals(1, configs.size());
        assertEquals("smarthome_sensor_100", configs.get(0).get("unique_id"));
        assertEquals("smarthome/sensor/100/state", configs.get(0).get("state_topic"));
        assertEquals("Kotłownia", ((Map<?, ?>) configs.get(0).get("device")).get("suggested_area"));

        Map<String, Object> state = MqttTopics.sensorStatePayload(termometr);
        assertEquals(22.5f, state.get("temperature"));
        assertTrue(!state.containsKey("humidity"));
    }

    @Test
    void buildsHygrometerDiscoveryAndStateWithHumidity() {
        Higrometr higrometr = new Higrometr();
        higrometr.setId(101);
        higrometr.setNazwa("Salon");
        higrometr.setTemperatura(21.0f);
        higrometr.setHumidity(48);

        List<Map<String, Object>> configs = MqttTopics.sensorDiscoveryConfigs(higrometr, "smarthome", null);
        assertEquals(2, configs.size());
        assertEquals("smarthome_sensor_101", configs.get(0).get("unique_id"));
        assertEquals("smarthome_sensor_101_humidity", configs.get(1).get("unique_id"));

        Map<String, Object> state = MqttTopics.sensorStatePayload(higrometr);
        assertEquals(21.0f, state.get("temperature"));
        assertEquals(48, state.get("humidity"));
    }

    @Test
    void skipsSensorStateUntilFirstRealReading() {
        Termometr termometr = new Termometr();
        termometr.setId(102);
        assertNull(MqttTopics.sensorStatePayload(termometr));

        Higrometr higrometr = new Higrometr();
        higrometr.setId(103);
        higrometr.setTemperatura(20.0f);
        assertNull(MqttTopics.sensorStatePayload(higrometr));
    }

    @Test
    void buildsButtonEventDiscovery() {
        Button button = new Button(8, 3);
        button.setId(104);
        button.setNazwa("Przycisk przy drzwiach");

        List<Map<String, Object>> configs = MqttTopics.sensorDiscoveryConfigs(button, "smarthome", "Hol");
        assertEquals(1, configs.size());
        Map<String, Object> config = configs.get(0);
        assertEquals("smarthome_sensor_104", config.get("unique_id"));
        assertEquals("smarthome/sensor/104/event", config.get("state_topic"));
        assertEquals("button", config.get("device_class"));
        List<?> eventTypes = (List<?>) config.get("event_types");
        assertEquals(2 * MqttTopics.MAX_BUTTON_CLICKS, eventTypes.size());
        assertTrue(eventTypes.contains("click_1") && eventTypes.contains("hold_5"));
        assertEquals("Hol", ((Map<?, ?>) config.get("device")).get("suggested_area"));
        assertEquals("event", MqttTopics.haComponentForSensor(SensorsTypes.BUTTON));
    }

    @Test
    void mapsButtonCommandsToEventTypesFromDiscovery() {
        List<String> eventTypes = MqttTopics.buttonEventTypes();

        Map<String, Object> click = MqttTopics.buttonEventPayload(ButtonClickType.CLICKED, 2);
        assertEquals("click_2", click.get("event_type"));
        assertEquals(2, click.get("clicks"));
        assertTrue(eventTypes.contains(click.get("event_type")));

        Map<String, Object> hold = MqttTopics.buttonEventPayload(ButtonClickType.HOLDED, 1);
        assertEquals("hold_1", hold.get("event_type"));
        assertTrue(eventTypes.contains(hold.get("event_type")));

        // typów spoza event_types HA nie przyjmie - nie publikujemy ich
        assertNull(MqttTopics.buttonEventPayload(ButtonClickType.CLICKED, MqttTopics.MAX_BUTTON_CLICKS + 1));
        assertNull(MqttTopics.buttonEventPayload(ButtonClickType.CLICKED, 0));
        assertNull(MqttTopics.buttonEventPayload(ButtonClickType.HOLDING, 1));
        assertNull(MqttTopics.buttonEventPayload(null, 1));
    }

    @Test
    void listsObjectIdsOfAllSensorEntities() {
        assertEquals(Arrays.asList("smarthome_sensor_5"), MqttTopics.sensorObjectIds(5, SensorsTypes.BUTTON));
        assertEquals(Arrays.asList("smarthome_sensor_5", "smarthome_sensor_5_humidity"),
                MqttTopics.sensorObjectIds(5, SensorsTypes.THERMOMETR_HYGROMETR));
        assertTrue(MqttTopics.sensorObjectIds(5, SensorsTypes.MOTION).isEmpty());
    }
}
