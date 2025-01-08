package newsmarthome.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import newsmarthome.model.Room;
import newsmarthome.model.hardware.HardwareFactory;
import newsmarthome.model.hardware.device.Blind;
import newsmarthome.model.hardware.device.Device;
import newsmarthome.model.hardware.device.DeviceState;
import newsmarthome.model.hardware.device.DeviceTypes;
import newsmarthome.model.hardware.device.Fan;
import newsmarthome.model.hardware.device.Light;
import newsmarthome.model.hardware.device.Outlet;
import newsmarthome.model.hardware.sensor.Button;
import newsmarthome.model.hardware.sensor.ButtonClickType;
import newsmarthome.model.hardware.sensor.ButtonLocalFunction;
import newsmarthome.model.hardware.sensor.Higrometr;
import newsmarthome.model.hardware.sensor.Sensor;
import newsmarthome.model.hardware.sensor.SensorsTypes;
import newsmarthome.model.hardware.sensor.Termometr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Repository("WiFiSlaveDAO")
@DependsOn("hardwareFactory")
public class WifiSlaveDAO {
    Logger logger;

    HardwareFactory hardwareFactory;

    
    public WifiSlaveDAO(@Autowired HardwareFactory hardwareFactory) {
        this.hardwareFactory = hardwareFactory;
        logger = LoggerFactory.getLogger(WifiSlaveDAO.class);
    }

}