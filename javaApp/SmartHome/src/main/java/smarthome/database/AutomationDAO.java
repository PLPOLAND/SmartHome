package smarthome.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import smarthome.automation.AutomationFunction;
import smarthome.automation.ButtonFunction;
import smarthome.automation.Function;
import smarthome.automation.FunctionAction;
import smarthome.automation.UserFunction;
import smarthome.model.hardware.Button;
import smarthome.model.hardware.Device;

@Repository
public class AutomationDAO {

    private SystemDAO systemDAO;

    private static final String FILES_LOCATION = "smarthome/database/automation/";

    HashMap<Integer, Function> functions;

    ArrayList<ButtonFunction> buttonFunctions;
    ArrayList<AutomationFunction> automationFunctions;
    ArrayList<UserFunction> userFunctions;

    Logger logger;

    public AutomationDAO(@Autowired SystemDAO systemDAO) {
        logger = LoggerFactory.getLogger(this.getClass());
        functions = new HashMap<>();
        buttonFunctions = new ArrayList<>();
        automationFunctions = new ArrayList<>();
        userFunctions = new ArrayList<>();
        this.systemDAO = systemDAO;
        try {
            readDatabase();
        } catch (IOException e) {
            logger.error("Error reading database: {}", e.getMessage());
        }
    }

    public void setSystemDAO(SystemDAO systemDAO) {
        this.systemDAO = systemDAO;
    }

    public void addFunction(Function function) {
        if (function.getId() == -1) {
            Integer newId = 0;
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                if (!functions.containsKey(i)) {
                    newId = i;
                    break;
                }
            }
            function.setId(newId);
        }

        if (functions.containsKey(function.getId())) {
            logger.error("Function with id {} already exists", function.getId());
        } else {
            functions.put(function.getId(), function);
            if (function instanceof ButtonFunction) {
                buttonFunctions.add((ButtonFunction) function);
            } else if (function instanceof AutomationFunction) {
                automationFunctions.add((AutomationFunction) function);
            } else if (function instanceof UserFunction) {
                userFunctions.add((UserFunction) function);
            }
            save(function);

        }
    }

    public void removeFunction(Function function) {
        functions.remove(function.getId());
        if (function instanceof ButtonFunction) {
            buttonFunctions.remove(function);
        } else if (function instanceof AutomationFunction) {
            automationFunctions.remove(function);
        } else if (function instanceof UserFunction) {
            userFunctions.remove(function);
        }
        deleteFunctionFile(function.getId());
    }

    public void removeFunction(int id) {
        if (functions.containsKey(id)) {
            removeFunction(functions.get(id));
        } else {
            logger.error("Function with id {} does not exist", id);
        }
    }

    public Function getFunction(int id) throws IllegalArgumentException {
        if (functions.containsKey(id)) {
            return functions.get(id);
        } else {
            logger.error("Function with id {} does not exist", id);
            throw new IllegalArgumentException("Function with id " + id + " does not exist");
        }
    }

    private void deleteFunctionFile(int id) {
        try {// tworzenie plików w plikach src
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            File projFile = new File(FILES_LOCATION + id + "_Function.json");
            projFile.getParentFile().mkdirs();
            projFile.delete();// usuń plik
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HashMap<Integer, Function> getAllFunctions() {
        return functions;
    }

    /**
     * 
     * @return list of all button functions
     */
    public List<ButtonFunction> getButtonFunctions() {
        return buttonFunctions;
    }

    /**
     * @return the automationFunctions
     */
    public List<AutomationFunction> getAutomationFunctions() {
        return automationFunctions;
    }

    /**
     * @return the userFunctions
     */
    public List<UserFunction> getUserFunctions() {
        return userFunctions;
    }

    public void save() {
        logger.debug("save all functions");
        for (Function fun : functions.values()) {
            save(fun);
        }
    }

    public void save(Function function) {
        logger.debug("save function");
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            File projFile = new File(FILES_LOCATION + function.getId() + "_Function.json");
            projFile.getParentFile().mkdirs();
            projFile.createNewFile();// utworzenie pliku jeśli nie istnieje
            objectMapper.writeValue(projFile, function);
        } catch (JsonGenerationException | JsonMappingException e) {
            logger.error("Error on saving function {} to file. -> {}", function, e);
        } catch (IOException e) {
            logger.error("Error on saving function {} to file. -> {}", function, e);
        }

    }

    /**
     * Czyta bazę danych z plików
     * 
     * @throws IOException
     */
    public void readDatabase() throws IOException {
        ObjectMapper obj = new ObjectMapper();
        obj.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        int i = 0;

        try (Stream<Path> paths = Files.walk(Paths.get(FILES_LOCATION))) {
            paths
                    .filter(Files::isRegularFile)
                    .forEach(filePath -> {
                        Function function = null;
                        try {
                            function = obj.readValue(
                                    new FileInputStream(new File(filePath.toString())), Function.class);
                            for (FunctionAction action : function.getActions()) {
                                Device dev = systemDAO.getDeviceByID(action.getDevice().getId());
                                action.setDevice(dev);
                            }
                            functions.put(function.getId(), function);
                            if (function instanceof ButtonFunction) {
                                ((ButtonFunction) function).setButton((Button) systemDAO
                                        .getSensorByID(((ButtonFunction) function).getButton().getId()));
                                buttonFunctions.add((ButtonFunction) function);
                            } else if (function instanceof AutomationFunction) {
                                automationFunctions.add((AutomationFunction) function);
                            } else if (function instanceof UserFunction) {
                                userFunctions.add((UserFunction) function);
                            }
                        } catch (JsonGenerationException | JsonMappingException e) {
                            logger.error("Error on reading function from file. -> {}", e.getMessage(), e);
                        } catch (IOException e) {
                            logger.info("Wczytano {} funkcji", i);
                        } catch (Exception e) {
                            logger.error("Błąd podczas wczytywania Funkcji", e);
                        }
                    });
        }
        logger.info("Wczytano {} funkcji", functions.size());
        // while (i < Integer.MAX_VALUE) {
        // Function function = null;
        // try {
        // function = obj.readValue(
        // new FileInputStream(new File(FILES_LOCATION + i + "_Function.json")),
        // Function.class);
        // for (FunctionAction action : function.getActions()) {
        // Device dev = systemDAO.getDeviceByID(action.getDevice().getId());
        // action.setDevice(dev);
        // }
        // functions.put(function.getId(), function);
        // if (function instanceof ButtonFunction) {
        // ((ButtonFunction) function).setButton((Button)
        // systemDAO.getSensorByID(((ButtonFunction) function).getButton().getId()));
        // buttonFunctions.add((ButtonFunction) function);
        // } else if (function instanceof AutomationFunction) {
        // automationFunctions.add((AutomationFunction) function);
        // } else if (function instanceof UserFunction) {
        // userFunctions.add((UserFunction) function);
        // }
        // i++;
        // } catch (JsonGenerationException | JsonMappingException e) {
        // logger.error("Error on reading function from file. -> {}", e.getMessage(),
        // e);
        // break;
        // } catch (IOException e) {
        // logger.info("Wczytano {} funkcji", i);

        // break;
        // } catch (Exception e) {
        // logger.error("Błąd podczas wczytywania Funkcji", e);
        // }
        // }
    }

    @Override
    public String toString() {
        return "AutomationDAO [functions=" + functions + ",\n\n buttonFunctions=" + buttonFunctions
                + ",\n\n automationFunctions=" + automationFunctions + ",\n\n userFunctions=" + userFunctions + "]";
    }

}
