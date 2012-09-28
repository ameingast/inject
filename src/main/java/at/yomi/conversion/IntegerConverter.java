package at.yomi.conversion;

public class IntegerConverter implements Converter<Integer> {
    @Override
    public Integer convert(String value) {
        return Integer.parseInt(value);
    }
}
