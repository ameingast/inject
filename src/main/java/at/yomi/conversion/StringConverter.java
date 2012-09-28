package at.yomi.conversion;

public class StringConverter implements Converter<String> {
    @Override
    public String convert(String value) {
        return value;
    }
}
