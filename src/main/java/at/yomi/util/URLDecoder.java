package at.yomi.util;

import java.io.UnsupportedEncodingException;

public class URLDecoder {
    private static final String UTF_8 = "UTF-8";

    public static String decode(String s) {
        return decode(s, UTF_8);
    }

    public static String decode(String s, String enc) {
        try {
            return java.net.URLDecoder.decode(s, enc);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private URLDecoder() {
        // Hide constructor
    }
}
