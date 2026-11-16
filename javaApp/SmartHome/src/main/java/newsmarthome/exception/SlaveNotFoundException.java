package newsmarthome.exception;


public class SlaveNotFoundException extends Exception{
    final int[] response;
    /**
     * 
     * @param errorMsg - wiadomość błędu
     * @param response - odpowiedź z urządzenia
     */
    public SlaveNotFoundException(String errorMsg, int[] response){
        super(errorMsg);
        this.response = response;
    }

    public SlaveNotFoundException(String errorMsg, int[] response, Throwable t){
        super(errorMsg,t);
        this.response = response;
    }

    public SlaveNotFoundException(String errorMsh, byte[] response){
        super(errorMsh);
        this.response = new int[response.length];
        for(int i = 0; i < response.length; i++){
            this.response[i] = response[i];
        }
    }

    public SlaveNotFoundException(String errorMsg){
        super(errorMsg);
        this.response = null;
    }
    public SlaveNotFoundException(String errorMsg, Throwable t){
        super(errorMsg,t);
        this.response = null;
    }

    public int[] getResponse(){
        return this.response;
    }
}
