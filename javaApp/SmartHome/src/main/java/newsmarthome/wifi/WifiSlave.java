package newsmarthome.wifi;

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
public class WifiSlave {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    String ip;
    String mac;
    boolean connected;
    @JsonIgnore
    @Transient
    Socket socket;
    
    public WifiSlave(){
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

}
