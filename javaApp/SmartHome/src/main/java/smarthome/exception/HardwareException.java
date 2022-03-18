package smarthome.exception;

public class HardwareException extends Exception{
    public HardwareException(String errorMsg){
        super(errorMsg);
    }
    public HardwareException(String errorMsg, Throwable t){
        super(errorMsg,t);
    }
}
