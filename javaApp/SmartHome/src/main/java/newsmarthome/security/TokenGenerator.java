package newsmarthome.security;

/**
 * The TokenGenerator class generates a token based on an email and password using a hash function.
 */
public class TokenGenerator {
    private TokenGenerator(){}
    /**
     * Generuje token na podstawie hasła i emaila
     * @param nickname
     * @param password
     * @return token
     */
    static String generateToken(String nickname, String password){
        return Hash.hash(password) + Hash.hash(nickname)+Hash.hash(System.currentTimeMillis()+"");
    }
}
