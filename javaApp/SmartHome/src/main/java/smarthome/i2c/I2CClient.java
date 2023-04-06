package smarthome.i2c;

import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class I2CClient {
    private static final Logger logger = LoggerFactory.getLogger(I2CClient.class);

    I2CClient(String ip, int port) {
        clientSocket = null;
        out = null;
        in = null;
        this.startConnection(ip, port);// TODO check if connection was successful
        if (isConnected()) {
            processThread.start();
            recievingThread.start(); // TODO: implement recieving then uncomment
        }
    }

    private Socket clientSocket; // the socket that connects to the server
    private PrintWriter out; // the output stream to the server
    private BufferedReader in; // the input stream from the server

    private Random random = new Random(System.currentTimeMillis());

    private List<I2CCommand> toProcess = new ArrayList<>();

    /**
     * Process the command by sending it to the server and recieving when it is done
     */
    Thread processThread = new Thread(new Runnable() {

        private String sendCommand(I2CCommand command) {
            return sendMessage(command.toJSONString());
        }

        private String sendMessage(String msg) {
            try {
                if (clientSocket == null || clientSocket.isClosed()) {
                    return null;
                }
                out.println(msg);
                // String resp = in.readLine();
                // I2CResponse response = I2CResponse.fromJSONString(resp);
                // if (response != null) {
                // if (response.getId() == -1) {
                // System.out.println("Error: " + response);
                // } else {
                // System.out.println("Response: " + response);
                // recieved.add(response);
                // }
                // }
                String resp = "";
                return resp;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void run() {
            while (isConnected()) {
                try {
                    Thread.sleep(1);// sleep for 1 ms to prevent cpu overload
                    if (!toProcess.isEmpty()) {
                        for (I2CCommand i2cCommand : toProcess) {
                            switch (i2cCommand.getState()) {
                                case CREATED:
                                    sendCommand(i2cCommand);
                                    i2cCommand.setState(I2CCommand.State.SENT);
                                    break;
                                case SENT:
                                case RECIEVED:
                                    // do nothing
                                    break;
                                case PROCESSED:
                                    toProcess.remove(i2cCommand); // remove the command from the list
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                } catch (InterruptedException e) {

                    logger.error("processThread interrupted while sleeping", e);
                }
            }
            logger.info("processThread stopped");
        }
    }, "I2CClient-processThread");

    /**
     * This function returns an I2CCommand object from a list based on its ID.
     * 
     * @param id The parameter "id" is an integer value representing the ID of the
     *           I2C command that
     *           needs to be retrieved from the commands list.
     * @return `I2CCommand` object with the specified ID or null if no such command
     *         exists.
     */
    public I2CCommand getCommand(int id) {
        for (I2CCommand i2cCommand : toProcess) {
            if (i2cCommand.getId() == id) {
                return i2cCommand;
            }
        }
        return null;
    }

    /**
     * Thread that recieves messages from the server and processes them.
     * 
     */
    Thread recievingThread = new Thread(() -> {
        while (isConnected()) {
            String resp;
            try {
                resp = in.readLine();
                I2CResponse response = I2CResponse.fromJSONString(resp);
                if (response != null) {
                    if (response.getId() == -1) {
                        logger.debug("Error: {}", response);
                    } else {
                        logger.debug("Got: Response: {}", response);
                        I2CCommand command = getCommand(response.getId());
                        if (command != null) {
                            command.setState(I2CCommand.State.RECIEVED);
                            command.setResponse(response);
                            logger.debug("Command: {}", command);
                        } else {
                            logger.error("Error: Command with id {} not found!", response.getId());
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }, "I2CClient-recievingThread");

    /**
     * This function attempts to start a connection with a server at a specified IP
     * address and port,
     * and returns true if successful, false otherwise.
     * 
     * @param ip   The IP address of the server that the client wants to connect to.
     * @param port The port parameter is an integer that specifies the port number
     *             on which the client
     *             will connect to the server. It is used in the Socket constructor
     *             to create a new socket object
     *             that connects to the specified IP address and port number.
     * @return A boolean value indicating whether the connection was successfully
     *         established or not.
     */
    private boolean startConnection(String ip, int port) {
        try {
            clientSocket = new Socket(ip, port);

            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            return true;
        } catch (java.net.ConnectException exception) {
            logger.error("Connection refused. You need to initiate the server first.", exception);
            return false;
            // TODO: add a way to start the server from here
        } catch (Exception e) {
            logger.error("Error while connecting to server", e);
            return false;
        }
    }

    /**
     * The function checks if the client socket is connected.
     * 
     * @return A boolean value indicating whether the clientSocket is connected.
     */
    public boolean isConnected() {
        return clientSocket != null && clientSocket.isConnected();
    }

    /**
     * This function sends a command with a randomly generated ID to a specified
     * address and adds it to
     * a list of commands to be sent.
     * 
     * @param command A Byte array representing the command to be sent over I2C
     *                communication.
     * @param address The address parameter is an integer value representing the
     *                address of the device
     *                to which the command is being sent.
     * @return An integer value representing the ID of the command.
     */
    public int sendCommand(Byte[] command, int address) {
        int id = random.nextInt(); // generate a random ID
        boolean found = false; // check if the ID is already used
        if (!toProcess.isEmpty()) { // if the list is not empty
            for (I2CCommand cmd : toProcess) { // check if the ID is already used
                if (cmd.getId() == id) {
                    found = true;
                    break;
                }
            }
        }
        while (found) { // if the ID is already used, generate a new one
            id = random.nextInt();
            found = false;
            for (I2CCommand cmd : toProcess) { // check if the ID is already used
                if (cmd.getId() == id) {
                    found = true;
                    break;
                }
            }
        }

        toProcess.add(new I2CCommand(id, command, address)); // add the command to the list of commands to be sent
        return id; // return the ID of the command
    }

    /**
     * This function closes the input and output streams and the client socket.
     */
    public void stopConnection() {
        try {
            if (in != null) {
                in.close();
                in = null;
            }
            if (out != null) {
                out.close();
                out = null;
            }
            if (clientSocket != null) {
                clientSocket.close();
                clientSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {// for test purposes
        Logger logger = LoggerFactory.getLogger(I2CClient.class);
        I2CClient i2c = new I2CClient("raspi4", 9803);
        if (i2c.isConnected()) {
            logger.debug("Connection established!");
            for (int i = 0; i < 20; i++) {
                I2CCommand command = new I2CCommand(i, new Byte[] { 'T', 'A' }, 0x08);
                logger.debug("{}",command.toJSONString());
                i2c.toProcess.add(command);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    logger.error("Error while sleeping", e);
                }
            }
        } else {
            System.out.println("Connection failed!");
        }
        while (!i2c.toProcess.isEmpty()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                logger.error("Error while sleeping", e);
            }
        }
        logger.debug("toSent is empty");
        i2c.stopConnection();
    }

}
