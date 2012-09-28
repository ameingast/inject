package at.yomi.conversion;

public class BooleanConverter implements Converter<Boolean> {
    @Override
    public Boolean convert(String value) {
        return Boolean.valueOf(value);
    }
}
