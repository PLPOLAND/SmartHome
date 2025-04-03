package newsmarthome.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import newsmarthome.database.repository.WifiSlaveRepository;
import newsmarthome.model.hardware.HardwareFactory;
import newsmarthome.wifi.ClientToWiFiSlaveMapper;
import newsmarthome.wifi.WifiSlave;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Repository("wiFiSlaveDAO")
// @DependsOn("hardwareFactory", "wifiSlaveRepository")
@Log4j2
@Data
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
    public WifiSlave getWifiSlaveById(int id) {
        for (WifiSlave wifiSlave : wifiSlaves) {
            if (wifiSlave.getId() == id) {
                return wifiSlave;
            }
        }
        return null;
    }
    public WifiSlave getWifiSlaveByMac(String mac) {
        for (WifiSlave wifiSlave : wifiSlaves) {
            if (wifiSlave.getMac().equals(mac)) {
                return wifiSlave;
            }
        }
        return null;
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