package at.yomi.conversion;

public class ConversionException extends RuntimeException {
    public ConversionException(String msg, String value) {
        super("Error while converting '" + value + "' into 'null': " + msg);
    }

    public ConversionException(String msg, String value, Class<?> type) {
        super("Error while converting '" + value + "' into '" + type.getName() + "': " + msg);
    }

    public ConversionException(String msg) {
        super(msg);
    }
}
