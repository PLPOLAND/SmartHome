package smarthome.security;

/**
 * Hash
 */
public class Hash {

    static private String hashWord = "onomatopejakowalskiego";
    /**
     * Hashuje hasło
     * @param word - hasło do zahashowania 
     * @return zahashowane hasło
     */
    static public String hash(String word) {
        String result = new String();
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
    /**
     * wypisuje zahashowane hasło
     */
    // public static void main(String[] args) {
    //     System.out.println(hash("xxx"));
    // }
}