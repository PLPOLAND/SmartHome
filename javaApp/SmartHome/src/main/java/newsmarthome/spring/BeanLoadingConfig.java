package newsmarthome.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import newsmarthome.database.SystemDAO;
import newsmarthome.i2c.I2CHardware;
import newsmarthome.i2c.MasterToSlaveConverter;
import newsmarthome.model.hardware.HardwareFactory;

@Configuration
public class BeanLoadingConfig {

    // @Bean(name = "hardwareFactory")
    // @DependsOn("masterToSlaveConverter")
    // HardwareFactory hardwareFactory(){
    //     return new HardwareFactory();
    // }

    // @Bean(name = "masterToSlaveConverter")
    // @DependsOn("I2CHardware")
    // MasterToSlaveConverter masterToSlaveConverter(){
    //     return new MasterToSlaveConverter();
    // }

    // @Bean(name = "I2CHardware")
    // I2CHardware i2CHardware(){
    //     return new I2CHardware();
    // }

    // @Bean(name = "systemDAO")
    // @DependsOn("hardwareFactory")
    // SystemDAO systemDAO(){
    //     return new SystemDAO();
    // }


    

}
