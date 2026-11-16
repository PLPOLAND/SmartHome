package newsmarthome.service;

/** Odrzucona zmiana konfiguracji - komunikat trafia do użytkownika (REST / formularz w Home Assistant). */
public class ConfigurationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ConfigurationException(String message) {
        super(message);
    }
}
