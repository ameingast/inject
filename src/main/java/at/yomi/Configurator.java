package at.yomi;

import at.yomi.util.ReflectUtil;

import java.lang.reflect.Field;
import java.util.*;

import static at.yomi.util.ReflectUtil.getFieldsWithAnnotation;

class Configurator {
    private final Map<String, Map<String, Object>> configCache = new HashMap<String, Map<String, Object>>();

    private String context;

    private Map<Class<?>, Set<Field>> fieldCache = new HashMap<Class<?>, Set<Field>>();

    public void configure(Object obj) {
        if (obj == null) {
            return;
        }
        loadConfig(obj.getClass());
        injectConfiguration(obj);
    }

    public void setContext(String context) {
        this.context = context;
    }

    private void checkRequired(String className, Properties bundle, Config cfgAnn, String fieldName) {
        if (cfgAnn.required()) {
            if (!bundle.containsKey(fieldName)) {
                throw new ConfigurationException("Missing configuration", className, fieldName);
            }
        }
    }

    private Set<Field> getConfigFields(Class<?> klass) {
        if (!fieldCache.containsKey(klass)) {
            fieldCache.put(klass, ReflectUtil.getFieldsWithAnnotation(klass, Config.class));
        }
        return fieldCache.get(klass);
    }

    private void injectConfiguration(Object obj) {

        Class<?> klass = obj.getClass();

        Map<String, Object> configs = configCache.get(klass.getName());
        Set<Field> fields = getConfigFields(klass);
        for (Field field : fields) {
            if (configs.containsKey(field.getName())) {
                field.setAccessible(true);
                try {
                    field.set(obj, configs.get(field.getName()));
                } catch (Exception e) {
                    Config cfg = field.getAnnotation(Config.class);
                    if (!cfg.ignoreExceptions()) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private void loadConfig(Class<?> klass) {
        String className = klass.getName();

        if (!configCache.containsKey(className)) {
            Set<Field> fields = getFieldsWithAnnotation(klass, Config.class);
            Map<String, Object> classMap = new HashMap<String, Object>();

            loadConfigFromBundle(className, fields, classMap);

            configCache.put(className, Collections.unmodifiableMap(classMap));
        }
    }

    private void loadConfigFromBundle(String className, Set<Field> fields, Map<String, Object> classMap) {
        Properties props = new Properties();

        String cpFile = className.replaceAll("\\.", "/") + ".properties";

        try {
            props.load(this.getClass().getClassLoader().getResourceAsStream(cpFile));
        } catch (Exception e) {
            // ignore
        }

        for (Field field : fields) {
            Config cfgAnn = field.getAnnotation(Config.class);
            String fieldName = field.getName();

            checkRequired(className, props, cfgAnn, fieldName);

            try {
                String value = props.getProperty(fieldName);
                Object convertedValue = Convert.convert(value, field.getType());
                if (convertedValue != null) {
                    classMap.put(fieldName, convertedValue);
                }
            } catch (Throwable e) {
                if (!cfgAnn.ignoreExceptions()) {
                    ConfigurationException ex = new ConfigurationException("Error in loading config", className,
                            fieldName);
                    ex.initCause(e);
                    throw ex;
                }
            }
        }
    }
}
