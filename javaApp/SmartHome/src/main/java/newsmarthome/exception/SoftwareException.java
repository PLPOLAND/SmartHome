package newsmarthome.exception;

public class SoftwareException extends Exception{

    final String expected;
    final String got;

    public SoftwareException(String errorMsg, String expected, String got){
        super(errorMsg+ " Expected: " + expected + " Got: " + got);
        this.expected = expected;
        this.got = got;
    }

    public SoftwareException(String errorMsg){
        super(errorMsg);
        this.expected = null;
        this.got = null;
    }
    public SoftwareException(String errorMsg, Throwable t){
        super(errorMsg,t);
        this.expected = null;
        this.got = null;        
    }

    public String getExpected(){
        return this.expected;
    }

    public String getGot(){
        return this.got;
    }
}
