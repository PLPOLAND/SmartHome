package newsmarthome.runners;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WiFiDeviceRunner {
    
    
    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(WiFiDeviceRunner.class);
        final int port = 1998;
        try {
            ServerSocket server = new ServerSocket(port);
            while (true) {
                Socket clientSocket = server.accept();
                logger.info("New client connected");
                logger.info("Client IP: " + clientSocket.getInetAddress().getHostAddress());
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                // Read and process client requests
                String clientMessage;
                while ((clientMessage = in.readLine()) != null) {
                    System.out.println("Received from client: " + clientMessage);
                    String response = "Server response: " + clientMessage.toUpperCase();
                    out.println(response);
                }

                // Close the streams and socket for this client
                in.close();
                out.close();
                clientSocket.close();
                System.out.println("Client disconnected");
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

}
