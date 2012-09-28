package at.yomi.conversion;

public interface Converter<T> {
    T convert(String value);
}
