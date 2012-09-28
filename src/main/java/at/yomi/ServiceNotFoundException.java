package at.yomi;

import java.lang.reflect.Field;

public class ServiceNotFoundException extends RuntimeException {
    public ServiceNotFoundException(Field field) {
        super("Service not found [class=" + field.getDeclaringClass().getName() + ", field=" + field.getName()
                + ", service=" + field.getType() + "]");
    }
}
