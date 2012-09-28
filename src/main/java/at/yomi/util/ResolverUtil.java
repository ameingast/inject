package at.yomi.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import static at.yomi.util.URLDecoder.decode;

public class ResolverUtil<T> {
    private static interface Test {
        boolean matches(Class<?> type);
    }

    private static class EndsWithServiceOrExtensionTest implements Test {
        private String[] ext = {"Service", "Extension"};

        @Override
        public boolean matches(Class<?> type) {
            for (String extension : ext) {
                if (type.getName().endsWith(extension) && !type.getName().endsWith(this.getClass().getSimpleName())) {
                    return true;
                }
            }
            return false;
        }
    }

    private Set<Class<? extends T>> matches = new HashSet<Class<? extends T>>();

    public ResolverUtil<T> find(Test test, String pckName) {

        String packageName = pckName.replace('.', '/');
        ClassLoader loader = getClassLoader();
        Enumeration<URL> urls;

        try {
            urls = loader.getResources(packageName);
        } catch (IOException ioe) {
            return this;
        }

        while (urls.hasMoreElements()) {
            String urlPath = urls.nextElement().getFile();
            urlPath = decode(urlPath);

            // If it's a file in a directory, trim the stupid file: spec
            if (urlPath.startsWith("file:")) {
                urlPath = urlPath.substring(5);
            }

            // Else it's in a JAR, grab the path to the jar
            if (urlPath.indexOf('!') > 0) {
                urlPath = urlPath.substring(0, urlPath.indexOf('!'));
            }

            File file = new File(urlPath);
            if (file.isDirectory()) {
                loadImplementationsInDirectory(test, packageName, file);
            } else {
                loadImplementationsInJar(test, packageName, file);
            }
        }

        return this;
    }

    public ResolverUtil<T> findServices(String... packageNames) {
        if (packageNames == null || packageNames.length == 0) {
            return this;
        }

        Test test = new EndsWithServiceOrExtensionTest();
        for (String pkg : packageNames) {
            find(test, pkg);
        }
        return this;
    }

    public Set<Class<? extends T>> getClasses() {
        return matches;
    }

    public ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    private void loadImplementationsInDirectory(Test test, String parent, File location) {
        File[] files = location.listFiles();

        for (File file : files) {

            StringBuilder builder = new StringBuilder(100);
            builder.append(parent).append("/").append(file.getName());
            String packageOrClass = builder.toString();

            if (file.isDirectory()) {
                loadImplementationsInDirectory(test, packageOrClass, file);
            } else if (file.getName().endsWith(".class")) {
                addIfMatching(test, packageOrClass);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void addIfMatching(Test test, String fqn) {
        try {
            String externalName = fqn.substring(0, fqn.indexOf('.')).replace('/', '.');
            ClassLoader loader = getClassLoader();

            Class<?> type = loader.loadClass(externalName);
            if (test.matches(type)) {
                matches.add((Class<? extends T>) type);
            }
        } catch (Throwable t) {
            // Ignore
        }
    }

    protected void loadImplementationsInJar(Test test, String parent, File jarfile) {
        JarInputStream jarStream = null;
        try {
            JarEntry entry;
            jarStream = new JarInputStream(new FileInputStream(jarfile));

            while ((entry = jarStream.getNextJarEntry()) != null) {
                String name = entry.getName();
                if (!entry.isDirectory() && name.startsWith(parent) && name.endsWith(".class")) {
                    addIfMatching(test, name);
                }
            }
        } catch (IOException ioe) {
            // Ignore
        } finally {
            if (jarStream != null) {
                try {
                    jarStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
