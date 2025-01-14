package newsmarthome.wifi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Data
@Entity
@Table(schema = "smarthome", name = "wifi_slaves")
public class WifiSlave implements Runnable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    String ip;
    String mac;
    boolean connected;
    @JsonIgnore
    @Transient
    Socket socket;

    public WifiSlave() {
        this.connected = false;
        this.socket = null;
        this.ip = "";
        this.mac = "";
    }

    public WifiSlave(int id, String ip, String mac) {
        this.id = id;
        this.ip = ip;
        this.mac = mac;
        this.connected = false;
        this.socket = null;
    }

    public WifiSlave(int id, String ip, String mac, Socket socket) {
        this.id = id;
        this.ip = ip;
        this.mac = mac;
        this.socket = socket;
        this.connected = true;
    }

    @Override
    public void run() {
        PrintWriter out = null;
        BufferedReader in = null;
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Połączono z klientem");

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Otrzymano: " + inputLine);
                out.println("Serwer: " + inputLine);
            }
        } catch (IOException e) {
            System.out.println("Błąd komunikacji z klientem: " + e.getMessage());
        } finally {
            this.connected = false;
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
