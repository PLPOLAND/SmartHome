package newsmarthome.wifi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Data
@Entity
@Table(schema = "smarthome", name = "wifi_slaves")
@Log4j2
public class WifiSlave implements Runnable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String ip;
    private String mac;
    private boolean connected;

    @JsonIgnore
    @Transient
    private Socket socket;
    @JsonIgnore
    @Transient 
    private PrintWriter out;   
    @JsonIgnore
    @Transient 
    private BufferedReader in;

    @Transient
    @JsonIgnore
    private Thread outThread;
    
    @JsonIgnore
    public boolean isConnected() {
        if (socket == null) {
            connected = false;
            return connected;
        }
        connected = !socket.isClosed() && socket.isConnected();
        return connected;
    }

    public WifiSlave() {
        this.connected = false;
        this.socket = null;
        this.ip = "";
        this.mac = "";
        this.out = null;
        this.in = null;
    }

    public WifiSlave(int id, String ip, String mac) {
        this.id = id;
        this.ip = ip;
        this.mac = mac;
        this.connected = false;
        this.socket = null;
        this.out = null;
        this.in = null;
    }

    public WifiSlave(int id, String ip, String mac, Socket socket, PrintWriter out, BufferedReader in) {
        this.id = id;
        this.ip = ip;
        this.mac = mac;
        this.socket = socket;
        this.connected = true;
        this.out = out;
        this.in = in;
    }

    
    @Override
    public void run() {
        log.info("Uruchomiono wątek dla klienta: {}", id);
        if (socket == null) {
            log.error("Socket is null");
            return;
        }
        if (socket.isClosed()) {
            log.error("Socket is closed");
            return;
        }
        try {
            startPinging();
            String inputLine;
            while (true) {
                if (socket.isClosed()) {
                    log.error("Socket is closed");
                    break;
                }
                if((inputLine = in.readLine()) != null) {
                    if (inputLine.equals("pong")) {
                        log.info("Otrzymano: pong");
                        continue;
                    }
                    //TODO handle input
                    log.info("Otrzymano: {}", inputLine);
                }
            }
        } catch (IOException e) {
            log.error("Błąd komunikacji z klientem: " + e.getMessage());
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

    /**
     * Starts a new thread that continuously sends "ping" messages to the output stream.
     * The thread will run indefinitely until the socket is closed.
     * If the socket is closed, an error message is logged and the thread terminates.
     * The thread sleeps for 1 second between each ping message.
     */
    private void startPinging() {
        outThread = new Thread(() -> {
            while (true) {
                if (socket.isClosed()) {
                    log.error("Socket is closed");
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.error("Thread interrupted: {}", e.getMessage());
                }
                out.println("ping");
                out.flush();
            }
        });
        outThread.start();
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WifiSlave wifiSlave = (WifiSlave) o;
        return id.equals(wifiSlave.id);
    }
}
