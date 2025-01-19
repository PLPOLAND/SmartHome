package newsmarthome.wifi;

import java.io.*;
import java.net.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import newsmarthome.database.repository.WifiSlaveRepository;
@Service
public class WiFiServer {


    final WifiSlaveRepository wifiSlaveRepository;

    
    public WiFiServer(@Autowired WifiSlaveRepository wifiSlaveRepository) {
        this.wifiSlaveRepository = wifiSlaveRepository;
    }

    // @Scheduled(fixedRate = 100)
    // public void task() {
    //     int port = 9803; // Port na którym serwer będzie nasłuchiwał

    //     try (ServerSocket serverSocket = new ServerSocket(port)) {
    //         System.out.println("Serwer nasłuchuje na porcie " + port);

    //         while (true) {
    //             Socket client = serverSocket.accept();
    //             System.out.println("New client connected"
    //                     + client.getInetAddress()
    //                             .getHostAddress());
    //             ClientToWiFiSlaveMapper clientSock = new ClientToWiFiSlaveMapper(client, wifiSlaveRepository);
    //             new Thread(clientSock).start();
    //         }
    //     } catch (IOException e) {
    //         System.out.println("Błąd uruchomienia serwera: " + e.getMessage());
    //     }
    // }
} 