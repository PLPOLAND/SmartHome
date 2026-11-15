package newsmarthome.mqtt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import newsmarthome.model.Room;
import newsmarthome.model.hardware.device.DeviceState;
import newsmarthome.model.hardware.device.DeviceTypes;
import newsmarthome.model.hardware.device.Light;
import newsmarthome.model.hardware.sensor.Button;
import newsmarthome.model.hardware.sensor.ButtonLocalFunction;
import newsmarthome.service.ClickFunctionSpec;
import newsmarthome.service.ConfigurationException;
import newsmarthome.service.ConfigurationService;

class MqttConfigHandlerTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private ConfigurationService service;
    private MqttConfigHandler handler;

    @BeforeEach
    void setUp() {
        service = mock(ConfigurationService.class);
        BridgeConfigPublisher snapshot = mock(BridgeConfigPublisher.class);
        when(snapshot.getMaxClickFunctions()).thenReturn(3);
        handler = new MqttConfigHandler(mock(MqttGateway.class), service, snapshot);
    }

    @Test
    void addsDeviceAndEchoesRequestId() throws Exception {
        Light light = new Light(DeviceState.OFF, 5, 18);
        light.setId(7);
        light.setName("Lampa");
        light.setRoom(1);
        when(service.addDevice("Lampa", DeviceTypes.LIGHT, 1, 18, 5, null)).thenReturn(light);

        JsonNode response = mapper.readTree(handler.handle("device/add",
                "{\"id\":\"abc\",\"name\":\"Lampa\",\"type\":\"LIGHT\",\"room\":1,\"slave\":18,\"pin\":5}"));

        assertEquals("abc", response.get("id").asText());
        assertEquals("ok", response.get("status").asText());
        assertEquals(7, response.get("data").get("id").asInt());
        assertEquals(5, response.get("data").get("pin").asInt());
        assertFalse(response.get("data").has("pin2"));
    }

    @Test
    void serviceRejectionBecomesErrorResponse() throws Exception {
        when(service.addRoom("Salon")).thenThrow(new ConfigurationException("Pokój o nazwie 'Salon' już istnieje"));

        JsonNode response = mapper.readTree(handler.handle("room/add", "{\"id\":1,\"name\":\"Salon\"}"));

        assertEquals(1, response.get("id").asInt());
        assertEquals("error", response.get("status").asText());
        assertEquals("Pokój o nazwie 'Salon' już istnieje", response.get("error").asText());
    }

    @Test
    void malformedRequestsAreRejectedWithoutCallingService() throws Exception {
        assertEquals("error", mapper.readTree(handler.handle("room/add", "{not json")).get("status").asText());
        assertEquals("error", mapper.readTree(handler.handle("room/add", "[1]")).get("status").asText());
        assertEquals("error", mapper.readTree(handler.handle("room/add", "{}")).get("status").asText());
        assertEquals("error",
                mapper.readTree(handler.handle("device/remove", "{\"id\":\"7\"}")).get("status").asText());
        JsonNode unknown = mapper.readTree(handler.handle("device/explode", "{\"id\":\"x\"}"));
        assertEquals("error", unknown.get("status").asText());
        assertEquals("x", unknown.get("id").asText());
        verify(service, never()).addRoom(any());
        verify(service, never()).removeDevice(anyInt());
    }

    @Test
    void partialSensorUpdatePassesOnlyGivenFields() throws Exception {
        Button button = new Button(18, 9);
        button.setId(12);
        when(service.updateSensor(eq(12), isNull(), isNull(), isNull(), isNull(), any())).thenReturn(button);

        JsonNode response = mapper.readTree(handler.handle("sensor/update",
                "{\"id\":12,\"click_functions\":[{\"clicks\":2,\"device\":7,\"state\":\"up\"}]}"));

        assertEquals("ok", response.get("status").asText());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ClickFunctionSpec>> captor = ArgumentCaptor.forClass(List.class);
        verify(service).updateSensor(eq(12), isNull(), isNull(), isNull(), isNull(), captor.capture());
        ClickFunctionSpec spec = captor.getValue().get(0);
        assertEquals(2, spec.getClicks());
        assertEquals(7, spec.getDeviceId());
        assertEquals(ButtonLocalFunction.State.UP, spec.getState());
    }

    @Test
    void clickFunctionsAboveLimitOrWithUnknownStateAreRejected() throws Exception {
        assertEquals("error", mapper.readTree(handler.handle("sensor/update",
                "{\"id\":12,\"click_functions\":[{\"clicks\":4,\"device\":7,\"state\":\"NONE\"}]}")).get("status").asText());
        assertEquals("error", mapper.readTree(handler.handle("sensor/update",
                "{\"id\":12,\"click_functions\":[{\"clicks\":1,\"device\":7,\"state\":\"TOGGLE\"}]}")).get("status").asText());
        verify(service, never()).updateSensor(anyInt(), any(), any(), any(), any(), any());
    }

    @Test
    void actionsWithoutDataReturnOkOnly() throws Exception {
        when(service.renameRoom(3, "Biuro")).thenReturn(new Room(3, "Biuro"));

        JsonNode removed = mapper.readTree(handler.handle("room/remove", "{\"id\":3}"));
        assertEquals("ok", removed.get("status").asText());
        assertNull(removed.get("data"));
        verify(service).removeRoom(3);

        assertEquals("Biuro",
                mapper.readTree(handler.handle("room/rename", "{\"id\":3,\"name\":\"Biuro\"}")).get("data").get("name").asText());
        assertTrue(mapper.readTree(handler.handle("bridge/restart_slaves", "")).get("status").asText().equals("ok"));
        verify(service).restartSlaves();
    }
}
