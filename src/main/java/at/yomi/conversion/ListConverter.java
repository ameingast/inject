package at.yomi.conversion;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class ListConverter implements Converter<List> {
    @Override
    public List<String> convert(String value) {
        value = value.trim();
        if (value.startsWith("[") && value.endsWith("]")) {
            value = value.substring(1, value.length() - 1);
        }

        try {
            // Set up a StreamTokenizer on the characters in this String
            StreamTokenizer st = new StreamTokenizer(new StringReader(value));
            st.whitespaceChars(',', ','); // Commas are delimiters
            st.ordinaryChars('0', '9'); // Needed to turn off numeric flag
            st.ordinaryChars('.', '.');
            st.ordinaryChars('-', '-');
            st.wordChars('0', '9'); // Needed to make part of tokens
            st.wordChars('.', '.');
            st.wordChars('-', '-');

            // Split comma-delimited tokens into a List
            ArrayList<String> list = new ArrayList<String>();
            while (true) {
                int ttype = st.nextToken();
                if (ttype == StreamTokenizer.TT_WORD || ttype > 0) {
                    list.add(st.sval);
                } else if (ttype == StreamTokenizer.TT_EOF) {
                    break;
                }
            }

            // Return the completed list
            return list;
        } catch (IOException e) {
            throw new ConversionException(value);
        }
    }
}
