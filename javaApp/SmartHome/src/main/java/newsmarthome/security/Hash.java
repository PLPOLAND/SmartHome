package newsmarthome.security;

/**
 * Hash
 */
public class Hash {

    private static String hashWord = "onomatopejakowalskiego";
    private Hash() {}
    /**
     * Hashuje hasło
     * @param word - hasło do zahashowania 
     * @return zahashowane hasło
     */
    public static String hash(String word) {
        String result = "";
        int j = 0;
        for (int i = 0; i < word.length(); i++) {
            result += (char)(((int)word.charAt(i) + (int)hashWord.charAt(j))%26 + 97);
            if(j+1<hashWord.length()){
                j++;
            }
            else{
                j = 0;
            }
        }
        return result;
    }
    public static void main(String[] args) {
        System.out.println(hash("Dziadunio"));
    }
}