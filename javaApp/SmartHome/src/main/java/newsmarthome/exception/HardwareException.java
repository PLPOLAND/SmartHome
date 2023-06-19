package newsmarthome.exception;

import java.lang.reflect.Array;

public class HardwareException extends Exception{
    final int[] response;
    /**
     * 
     * @param errorMsg - wiadomość błędu
     * @param response - odpowiedź z urządzenia
     */
    public HardwareException(String errorMsg, int[] response){
        super(errorMsg);
        this.response = response;
    }

    public HardwareException(String errorMsg, int[] response, Throwable t){
        super(errorMsg,t);
        this.response = response;
    }

    public HardwareException(String errorMsh, byte[] response){
        super(errorMsh);
        this.response = new int[response.length];
        for(int i = 0; i < response.length; i++){
            this.response[i] = response[i];
        }
    }

    public HardwareException(String errorMsg){
        super(errorMsg);
        this.response = null;
    }
    public HardwareException(String errorMsg, Throwable t){
        super(errorMsg,t);
        this.response = null;
    }

    public int[] getResponse(){
        return this.response;
    }
}
