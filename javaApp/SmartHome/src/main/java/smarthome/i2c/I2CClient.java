package smarthome.i2c;

import java.net.*;
import java.util.Arrays;
import java.util.Random;
import java.io.*;

public class I2CClient {
    

    I2CClient(){

    }

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public void startConnection(String ip, int port) {
        try{
            clientSocket = new Socket(ip, port);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public String sendMessage(String msg) {
        try {
            out.println(msg);
            // String resp = in.readLine();
            String resp = "";
            return resp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void stopConnection() {
        
        try {
            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {//for test purposes
        I2CClient i2c = new I2CClient();
        i2c.startConnection("raspi-1", 9803);
        for (int i = 0; i < 20; i++) {
            byte[] tmp = new byte[10];
            Random rand = new Random( 10);
            rand.nextBytes(tmp);
            System.out.println(i2c.sendMessage(Arrays.toString(tmp)));
        }
        i2c.stopConnection();
    }

}
