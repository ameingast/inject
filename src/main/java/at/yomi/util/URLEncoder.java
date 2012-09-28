package at.yomi.util;

import java.io.UnsupportedEncodingException;

public class URLEncoder {
    private static final String UTF_8 = "UTF-8";

    public static String encode(String s) {
        return encode(s, UTF_8);
    }

    public static String encode(String s, String enc) {
        try {
            return java.net.URLEncoder.encode(s, enc);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private URLEncoder() {
        // Hide constructor
    }
}
