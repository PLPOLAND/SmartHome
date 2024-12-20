package newsmarthome.wifi;

import java.io.*;
import java.net.*;

public class WiFiServer {

    public static void main(String[] args) {
        int port = 9803; // Port na którym serwer będzie nasłuchiwał

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Serwer nasłuchuje na porcie " + port);

            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("New client connected"
                        + client.getInetAddress()
                                .getHostAddress());
                ClientHandler clientSock = new ClientHandler(client);
                new Thread(clientSock).start();
            }
        } catch (IOException e) {
            System.out.println("Błąd uruchomienia serwera: " + e.getMessage());
        }
    }
}
class ClientHandler implements Runnable { 
        private final Socket clientSocket; 
  
        // Constructor 
        public ClientHandler(Socket socket) 
        { 
            this.clientSocket = socket; 
        } 
  
        public void run() 
        { 
            PrintWriter out = null;
            BufferedReader in = null;
                 try {
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                     in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    System.out.println("Połączono z klientem");

                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        System.out.println("Otrzymano: " + inputLine);
                        out.println("Serwer: " + inputLine);
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
                        clientSocket.close(); 
                    } 
                } 
                catch (IOException e) { 
                    e.printStackTrace(); 
                } 
            } 
        } 
    } 