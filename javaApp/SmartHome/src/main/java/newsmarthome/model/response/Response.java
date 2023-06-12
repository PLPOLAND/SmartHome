package newsmarthome.model.response;
/**
 *  Klasa przechowująca pola wysyłane do przeglądarki jako odpowiedź
 *  @author Marek Pałdyna
 */
public class Response<T> {
    String error;
    T response;

    public Response(T obj, String error){
        this.response = obj;
        this.error = error;
    }
    public Response(T obj){
        this.response = obj;
    }


    public String getError() {
        return this.error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public T getObj() {
        return this.response;
    }

    public void setObj(T obj) {
        this.response = obj;
    }


    @Override
    public String toString() {
        return "{" +
            " error='" + getError() + "'" +
            ", response='" + getObj() + "'" +
            "}";
    }

}
