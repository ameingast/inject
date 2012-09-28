package at.yomi.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class ReflectUtil {
    public static List<Class<?>> getActualTypeArguments(Type type) {
        List<Class<?>> arguments = new ArrayList<Class<?>>();
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            Type[] types = pt.getActualTypeArguments();
            if (types != null) {
                for (Type arg : types) {
                    arguments.add((Class<?>) arg);
                }
            }
        }
        return arguments;
    }

    public static Collection<Field> getFields(Class<?> klass) {
        Class<?> clazz = klass;
        Map<String, Field> fields = new HashMap<String, Field>();
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (!fields.containsKey(field.getName())) {
                    fields.put(field.getName(), field);
                }
            }

            clazz = clazz.getSuperclass();
        }

        return fields.values();
    }

    public static Set<Field> getFieldsWithAnnotation(Class<?> classToInspect, Class<? extends Annotation> annotation) {
        Set<Field> foundFields = new HashSet<Field>();

        Collection<Field> allFields = getFields(classToInspect);

        for (Field field : allFields) {
            if (field.isAnnotationPresent(annotation)) {
                foundFields.add(field);
            }
        }

        return foundFields;
    }

    public static Set<Field> getFieldsWithAnnotation(Object objectToInspect, Class<? extends Annotation> annotation) {
        return getFieldsWithAnnotation(objectToInspect.getClass(), annotation);
    }

    private ReflectUtil() {
        // Hide constructor
    }
}
