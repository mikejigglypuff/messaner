package messaner;

import org.springframework.stereotype.Component;

@Component
public class Utility {
    private final String[] sessionFormat = new String[]{"8", "-", "4", "-", "4", "-", "4", "-", "12"};
    private final String sep = "-";
    private final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public String createRandString(int len) {

        int charsNum = chars.length();

        StringBuilder sb = new StringBuilder();
        while (len-- > 0) {
            int randChar = (int)(Math.random() * charsNum);
            sb.append(chars.charAt(randChar));
        }
        return sb.toString();
    }

    public String createSessionId() {
        StringBuilder sb = new StringBuilder();
        for(String format : sessionFormat) {
            if(format.equals(sep)) {
                sb.append(sep);
            } else {
                sb.append(createRandString(Integer.parseInt(format)));
            }
        }

        return sb.toString();
    }
}
