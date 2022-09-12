package wallhaven;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pi4j.io.serial.Serial;

public class Wallhaven {
    // https://wallhaven.cc/api/v1/search?page=2&q=nature
    private static BufferedInputStream in;
    public static void main(String[] args) {
        String reply = "";

        try{
            in = new BufferedInputStream(new URL("https://wallhaven.cc/api/v1/search?q=nature&categories=100&atleast=1920x1080&sorting=favorites").openStream());
            reply = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> map = mapper.readValue(reply, Map.class);
            ArrayList<Map<String, Object>> images = (ArrayList<Map<String, Object>>)map.get("data");
            for (Map<String,Object> imageEntry : images) {
                saveImage(imageEntry);
            }
        }catch (Exception e){

        }

    }
    private static void saveImage(Map<String, Object> imageEntry) {
        FileOutputStream fileOutputStream = null;
        try {
            String path = (String) imageEntry.get("path");
            String [] slicedPath = path.split("/");
            in = new BufferedInputStream(new URL(path).openStream());
            fileOutputStream = new FileOutputStream(slicedPath[slicedPath.length-1]);
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}