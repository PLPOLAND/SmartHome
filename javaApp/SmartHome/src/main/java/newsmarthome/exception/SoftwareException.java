package newsmarthome.exception;

public class SoftwareException extends Exception{
    public SoftwareException(String errorMsg){
        super(errorMsg);
    }
    public SoftwareException(String errorMsg, Throwable t){
        super(errorMsg,t);
    }
}
