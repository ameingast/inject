package at.yomi.conversion;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class MapConverter implements Converter<Map> {
    @Override
    public Map<String, String> convert(String value) {
        value = value.trim();

        if (value.startsWith("{") && value.endsWith("}")) {
            value = value.substring(1, value.length() - 1);
        }

        Map<String, String> map = new HashMap<String, String>();
        try {
            StreamTokenizer st = new StreamTokenizer(new StringReader(value));
            st.whitespaceChars(':', ':');
            st.whitespaceChars(',', ',');
            st.ordinaryChars('0', '9'); // Needed to turn off numeric flag
            st.ordinaryChars('.', '.');
            st.ordinaryChars('-', '-');
            st.wordChars('0', '9'); // Needed to make part of tokens
            st.wordChars('.', '.');
            st.wordChars('-', '-');

            String key = null;
            while (true) {
                int ttype = 0;
                ttype = st.nextToken();

                if (ttype == StreamTokenizer.TT_WORD || ttype > 0) {
                    if (key == null) {
                        key = st.sval;
                    } else {
                        map.put(key, st.sval);
                        key = null;
                    }
                } else if (ttype == StreamTokenizer.TT_EOF) {
                    break;
                }
            }
        } catch (IOException e) {
            throw new ConversionException(value);
        }

        return map;
    }
}
