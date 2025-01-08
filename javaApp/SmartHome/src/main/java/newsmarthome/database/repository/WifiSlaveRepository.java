package newsmarthome.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import newsmarthome.wifi.WifiSlave;
import java.util.List;


@Repository
public interface WifiSlaveRepository extends JpaRepository<WifiSlave, Integer> {
    
    List<WifiSlave> findByConnected(boolean connected);
    List<WifiSlave> findByIp(String ip);
    List<WifiSlave> findByMac(String mac);
    
}
