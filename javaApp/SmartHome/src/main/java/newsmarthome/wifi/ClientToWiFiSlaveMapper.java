package newsmarthome.wifi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import newsmarthome.database.repository.WifiSlaveRepository;

@Log4j2
public class ClientToWiFiSlaveMapper implements Runnable { 
        private final Socket clientSocket; 
        private final WifiSlaveRepository wifiSlaveRepository;
        private WifiSlave wifiSlave;
        // Constructor 
        public ClientToWiFiSlaveMapper(Socket socket, WifiSlaveRepository wifiSlaveRepository) 
        { 
            this.clientSocket = socket; 
            this.wifiSlaveRepository = wifiSlaveRepository;
        }
  
        public void run() 
        { 
            boolean connected = true;
            PrintWriter out = null;
            BufferedReader in = null;
                 try {
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                    String mac;
                    mac = in.readLine();
                    if (mac==null) {
                        log.error("Got null from client");
                        connected = false;
                    }else{
                        log.debug("Got: " + mac);
                        List<WifiSlave> wifiSlaves = wifiSlaveRepository.findByMac(mac);
                        if (wifiSlaves.isEmpty()) {
                            log.debug("New slave connected");
                            wifiSlave = new WifiSlave();
                            wifiSlave.setMac(mac);
                            wifiSlave.setConnected(true);
                            wifiSlave.setSocket(clientSocket);
                            wifiSlave.setIp(clientSocket.getInetAddress().getHostAddress());
                            wifiSlaveRepository.save(wifiSlave);
                            wifiSlave.run();
                        } else {
                            log.debug("Slave reconnected");
                             wifiSlave = wifiSlaves.get(0);
                            wifiSlave.setConnected(true);
                            wifiSlave.setSocket(clientSocket);
                            wifiSlave.setIp(clientSocket.getInetAddress().getHostAddress());
                            wifiSlaveRepository.save(wifiSlave);
                            
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Błąd komunikacji z klientem: " + e.getMessage());
                }
            finally { 
                try { 
                    if (out != null) { 
                        out.close(); 
                    } 
                    if (in != null) { 
                        in.close(); 
                        if (!connected) {
                            clientSocket.close(); 
                        }
                    } 
                } 
                catch (IOException e) { 
                    e.printStackTrace(); 
                } 
            } 
        } 

        WifiSlave getWifiSlave() {
            return wifiSlave;
        }
    }