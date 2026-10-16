package newsmarthome.mqtt;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MqttGatewayTest {

    @Test
    void detectsPlainTcpToRemoteHost() {
        assertTrue(MqttGateway.isPlainTcpToRemoteHost("tcp://192.168.1.10:1883"));
        assertTrue(MqttGateway.isPlainTcpToRemoteHost("tcp://broker.example.com:1883"));
        assertFalse(MqttGateway.isPlainTcpToRemoteHost("tcp://localhost:1883"));
        assertFalse(MqttGateway.isPlainTcpToRemoteHost("tcp://127.0.0.1:1883"));
        assertFalse(MqttGateway.isPlainTcpToRemoteHost("tcp://[::1]:1883"));
        assertFalse(MqttGateway.isPlainTcpToRemoteHost("ssl://broker.example.com:8883"));
    }
}
