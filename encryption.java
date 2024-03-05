package MyChat;

import java.util.Base64;

class Encryption {
    static String encode(String originalString) {
        byte[] encodedString = Base64.getEncoder().encode(originalString.getBytes());

        return new String(encodedString);
    }

    static String decode(String encodedString) {
        byte[] decodedString = Base64.getDecoder().decode(encodedString.getBytes());

        return new String(decodedString);
    }
}