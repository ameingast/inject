package at.yomi;

import at.yomi.conversion.*;
import at.yomi.conversion.ConversionException;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Convert {
    private static Map<Class<?>, at.yomi.conversion.Converter<?>> converters = new HashMap<Class<?>, at.yomi.conversion.Converter<?>>();

    static {
        register(Integer.class, new IntegerConverter());
        register(Boolean.class, new BooleanConverter());
        register(Long.class, new LongConverter());
        register(String.class, new StringConverter());
        register(List.class, new ListConverter());
        register(Map.class, new MapConverter());
    }

    @SuppressWarnings("unchecked")
    public static <T> T convert(String value, Class<T> type) {
        if (type == null) {
            throw new ConversionException("Can't convert to null type", value);
        }

        if (value == null || value.equalsIgnoreCase("<NULL>")) {
            return null;
        }

        if (type.isArray()) {
            List<String> t = new ListConverter().convert(value);

            Object x = Array.newInstance(type.getComponentType(), t.size());
            for (int i = 0; i < t.size(); i++) {
                Array.set(x, i, convertSingle(t.get(i), type.getComponentType()));
            }
            return (T) x;
        }

        return convertSingle(value, type);
    }

    public static <T> void register(Class<T> type, at.yomi.conversion.Converter<T> instance) {
        converters.put(type, instance);
    }

    @SuppressWarnings("unchecked")
    private static <T> T convertSingle(String value, Class<T> type) {
        Converter<T> converter = (Converter<T>) converters.get(type);

        if (converter != null) {
            return converter.convert(value);
        }
        throw new ConversionException("No conversion found", value, type);
    }

    private Convert() {
        // Hide constructor
    }
}
