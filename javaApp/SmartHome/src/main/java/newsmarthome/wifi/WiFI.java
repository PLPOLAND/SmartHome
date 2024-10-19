package newsmarthome.wifi;

import java.io.*;
import java.net.*;

public class WiFI {

    public static void main(String[] args) {
        int port = 9803; // Port na którym serwer będzie nasłuchiwał

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Serwer nasłuchuje na porcie " + port);

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                    System.out.println("Połączono z klientem");

                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        System.out.println("Otrzymano: " + inputLine);
                        out.println("Serwer: " + inputLine);
                    }
                } catch (IOException e) {
                    System.out.println("Błąd komunikacji z klientem: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Błąd uruchomienia serwera: " + e.getMessage());
        }
    }
}
