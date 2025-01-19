package newsmarthome.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import newsmarthome.database.repository.WifiSlaveRepository;
import newsmarthome.model.Room;
import newsmarthome.model.hardware.HardwareFactory;
import newsmarthome.model.hardware.device.Blind;
import newsmarthome.model.hardware.device.Device;
import newsmarthome.model.hardware.device.DeviceState;
import newsmarthome.model.hardware.device.DeviceTypes;
import newsmarthome.model.hardware.device.Fan;
import newsmarthome.model.hardware.device.Light;
import newsmarthome.model.hardware.device.Outlet;
import newsmarthome.model.hardware.sensor.Button;
import newsmarthome.model.hardware.sensor.ButtonClickType;
import newsmarthome.model.hardware.sensor.ButtonLocalFunction;
import newsmarthome.model.hardware.sensor.Higrometr;
import newsmarthome.model.hardware.sensor.Sensor;
import newsmarthome.model.hardware.sensor.SensorsTypes;
import newsmarthome.model.hardware.sensor.Termometr;
import newsmarthome.wifi.ClientToWiFiSlaveMapper;
import newsmarthome.wifi.WifiSlave;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.extern.log4j.Log4j2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Repository("wiFiSlaveDAO")
// @DependsOn("hardwareFactory", "wifiSlaveRepository")
@Log4j2
public class WifiSlaveDAO {

    HardwareFactory hardwareFactory;
    final WifiSlaveRepository wifiSlaveRepository;
    List<WifiSlave> wifiSlaves = new ArrayList<>();

    
    public WifiSlaveDAO(@Autowired HardwareFactory hardwareFactory, @Autowired WifiSlaveRepository wifiSlaveRepository) {
        this.hardwareFactory = hardwareFactory;
        this.wifiSlaveRepository = wifiSlaveRepository;
        wifiSlaves = Collections.synchronizedList(new ArrayList<WifiSlave>());
        wifiSlaves.addAll(wifiSlaveRepository.findAll());
    }

    @Scheduled(fixedRate = 10)
	public void startServer() {
		int port = 9803; // Port na którym serwer będzie nasłuchiwał

		try (ServerSocket serverSocket = new ServerSocket(port)) {
			System.out.println("Serwer nasłuchuje na porcie " + port);

			while (true) {
				Socket client = serverSocket.accept();
				System.out.println("New client connected"
						+ client.getInetAddress()
								.getHostAddress());
				ClientToWiFiSlaveMapper clientSock = new ClientToWiFiSlaveMapper(client, wifiSlaveRepository);
				Thread th = new Thread(clientSock);
				th.start();
				new Thread(() -> {
					try {
						th.join();
						WifiSlave slave = clientSock.getWifiSlave();
                        if (!wifiSlaves.contains(slave)) {
                            wifiSlaves.add(slave);
                        }
                        else{
                            int index = wifiSlaves.indexOf(slave);
                            wifiSlaves.set(index, slave);
                        }
                        wifiSlaveRepository.save(slave);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}).start();
				//TODO after join add wifislave to list so we can use it in other methods
			}
		} catch (IOException e) {
			log.error("Błąd uruchomienia serwera: {}", e.getMessage());
		}
	}
    @Scheduled(fixedRate = 100)
    public void checkConnected() {
        for (WifiSlave wifiSlave : wifiSlaves) {
            // log.info("Checking: {} -> {}", wifiSlave.getId(), wifiSlave.getMac());
            if (wifiSlave!=null && !wifiSlave.isConnected()) {
                // log.info("{} disconected!", wifiSlave.getId());
                wifiSlaveRepository.save(wifiSlave);
            }
            else{
                // log.info("{} connected!", wifiSlave.getId());
            }
        }
    }

}