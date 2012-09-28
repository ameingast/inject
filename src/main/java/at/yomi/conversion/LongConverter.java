package at.yomi.conversion;

public class LongConverter implements Converter<Long> {
    @Override
    public Long convert(String value) {
        return Long.valueOf(value);
    }
}
