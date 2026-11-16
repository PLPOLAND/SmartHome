package newsmarthome.i2c;

import java.util.Arrays;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class I2CResponse {
    private final int id;
    private Byte[] responseBytes;
    private String responseString;

    public I2CResponse(int id, Byte[] responseBytes, String responseString) {
        this.id = id;
        this.responseBytes = responseBytes;
        this.responseString = responseString;
    }

    public int getId() {
        return id;
    }

    public Byte[] getResponseBytes() {
        return responseBytes;
    }

    public String getResponseString() {
        return responseString;
    }

    @Override
    public String toString() {
        return "I2CResponse [responseBytes=" + Arrays.toString(responseBytes) + ", responseString="
                + responseString + "]";
    }

    public String toJSONString(){
        return "{\"responseBytes\":"+Arrays.toString(responseBytes)+",\"responseString\":"+responseString+"}";
    }

    public static I2CResponse fromJSONString(String json){
        ObjectMapper ow = new ObjectMapper();
        ow.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        I2CResponse response = null;
        try {
            JsonNode jsonNodeRoot = ow.readTree(json);
            int id = jsonNodeRoot.get("id").asInt();
            Byte[] responseBytes = ow.readValue(jsonNodeRoot.get("responseBytes").toString(), Byte[].class); // TODO check if this works
            String responseString = jsonNodeRoot.get("responseString").asText();
            response = new I2CResponse(id, responseBytes, responseString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;

    }

}
