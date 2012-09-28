package at.yomi;

import at.yomi.util.ReflectUtil;
import at.yomi.util.ResolverUtil;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static at.yomi.util.ReflectUtil.getFieldsWithAnnotation;

public class Injector {

    private String context;

    private Map<Class<?>, Set<Field>> fieldCache = new ConcurrentHashMap<Class<?>, Set<Field>>();

    private ThreadLocal<List<ThreadLocal<?>>> threadFields = new ThreadLocal<List<ThreadLocal<?>>>() {
        @Override
        protected List<ThreadLocal<?>> initialValue() {
            return new ArrayList<ThreadLocal<?>>();
        }
    };

    final Configurator configurator = new Configurator();

    @Config
    String[] packages = {"at", "org", "com"};

    final Map<Class<?>, List<ServiceWrapper>> services = new ConcurrentHashMap<Class<?>, List<ServiceWrapper>>();

    public Injector() {
        this("default");
    }

    public Injector(String context) {
        this.context = context;
        configurator.setContext(this.context);
        configurator.configure(this);
    }

    public void inject(Object object) {
        if (object == null) {
            return;
        }

        configurator.configure(object);
        Collection<Field> fields = getInjectionFields(object);
        for (Field field : fields) {
            try {
                Class<?> type = field.getType();
                Object value = getCustom(field);
                if (value == null && type == Collection.class) {
                    injectAllServiceImpls(object, field);
                } else if (value == null && type.isInterface()) {
                    injectService(object, field);
                } else if (type == Lazy.class) {
                    injectObject(object, field, new Lazy<Object>(this, field));
                } else {
                    if (value == null && type != ThreadLocal.class) {
                        value = newInstance(field);
                    }
                    configurator.configure(value);
                    inject(value);
                    injectObject(object, field, value);
                }
            } catch (StackOverflowError e) {
                throw new StackOverflowError("Cyclic dependecy detected while injecting to: "
                        + object.getClass().getName() + "." + field.getName());
            }
        }
    }

    public void start() {
        buildServiceMap();

        for (List<ServiceWrapper> serviceGroup : services.values()) {
            for (ServiceWrapper wrapper : serviceGroup) {
                initialize(wrapper);
            }
        }
    }

    public void stop() {
        removeThreadObjects();
        for (List<ServiceWrapper> serviceGroup : services.values()) {
            for (ServiceWrapper wrapper : serviceGroup) {
                wrapper.stop();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void buildServiceMap() {
        Set<Class<?>> serviceClasses = (Set<Class<?>>) getServiceClasses();

        List<Class<?>> serviceInterfaces = new ArrayList<Class<?>>();
        List<Class<?>> serviceImplementations = new ArrayList<Class<?>>();

        for (Class<?> klass : serviceClasses) {
            if (klass.isInterface()) {
                serviceInterfaces.add(klass);
            } else {
                serviceImplementations.add(klass);
            }
        }

        for (Class<?> serviceInterface : serviceInterfaces) {
            List<ServiceWrapper> impls = new ArrayList<ServiceWrapper>();
            for (Class<?> current : serviceImplementations) {
                if (serviceInterface.isAssignableFrom(current)) {
                    try {
                        ServiceWrapper wrapper = new ServiceWrapper(current.newInstance());
                        boolean isDefault = current.getSimpleName().startsWith("Default");
                        if (!impls.isEmpty() && isDefault) {
                            wrapper = impls.set(0, wrapper);
                        }
                        impls.add(wrapper);
                    } catch (Exception e) {
                        // throw new ServiceNotFoundException(e);
                    }
                }
            }

            services.put(serviceInterface, impls);
        }
    }

    private Set<Field> getInjectionFields(Object object) {
        Class<?> klass = object.getClass();
        Set<Field> fields = fieldCache.get(klass);

        if (fields == null) {
            fields = getFieldsWithAnnotation(object, Inject.class);
            fieldCache.put(klass, fields);
        }

        return fields;
    }

    @SuppressWarnings("unchecked")
    private Set<?> getServiceClasses() {
        ResolverUtil<?> resolver = new ResolverUtil();
        resolver.findServices(packages);
        Set<?> serviceClasses = resolver.getClasses();
        return serviceClasses;
    }

    private void injectAllServiceImpls(Object object, Field field) {
        List<?> genericTypes = ReflectUtil.getActualTypeArguments(field.getGenericType());
        List<ServiceWrapper> impls = services.get(genericTypes.get(0));

        List<Object> implementations = new ArrayList<Object>();
        if (impls != null) {
            for (ServiceWrapper serviceWrapper : impls) {
                initialize(serviceWrapper);
                implementations.add(serviceWrapper.getInstance());
            }
        }

        injectObject(object, field, Collections.unmodifiableCollection(implementations));
    }

    private void initialize(ServiceWrapper wrapper) {
        inject(wrapper.getInstance());
        configurator.configure(wrapper.getInstance());
        wrapper.start();
    }

    @SuppressWarnings("unchecked")
    private void injectObject(Object object, Field field, Object value) {
        try {
            field.setAccessible(true);
            if (field.getType() == ThreadLocal.class) {
                ThreadLocal<Object> local = (ThreadLocal<Object>) field.get(object);
                if (local == null) {
                    local = new ThreadLocal<Object>();
                    field.set(object, local);
                    threadFields.get().add(local);
                }

                local.set(value);
            } else {
                field.set(object, value);
            }
        } catch (Exception e) {
            Inject injAnn = field.getAnnotation(Inject.class);
            if (injAnn.required()) {
                throw new RuntimeException(e);
            }
        }
    }

    private void injectService(Object object, Field field) {
        List<ServiceWrapper> wrappers = services.get(field.getType());

        if (wrappers == null || wrappers.isEmpty()) {
            Inject injAnn = field.getAnnotation(Inject.class);
            if (injAnn.required()) {
                throw new ServiceNotFoundException(field);
            }
            return;
        }

        injectToField(field, object, wrappers.get(0));
    }

    private void injectToField(Field field, Object instance, ServiceWrapper wrapper) {
        try {
            initialize(wrapper);
            injectObject(instance, field, wrapper.getInstance());
        } catch (Throwable e) {
            if (e instanceof StackOverflowError) {
                throw (StackOverflowError) e;
            }
        }
    }

    private Object newInstance(Field field) {
        try {
            return field.getType().newInstance();
        } catch (Exception e) {
            Inject injAnn = field.getAnnotation(Inject.class);
            if (injAnn.required()) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    protected Object getCustom(@SuppressWarnings("unused") Field field) {
        return null;
    }

    protected void removeThreadObjects() {
        for (ThreadLocal<?> tl : threadFields.get()) {
            tl.remove();
        }
        threadFields.remove();
    }

    @SuppressWarnings("unchecked")
    <T> T getService(Class<T> type, String hint) {
        List<ServiceWrapper> impls = services.get(type);

        if (impls != null) {
            for (ServiceWrapper serviceWrapper : impls) {
                if (serviceWrapper.supports(hint)) {
                    initialize(serviceWrapper);
                    return (T) serviceWrapper.getInstance();
                }
            }
        }
        return null;
    }
}
