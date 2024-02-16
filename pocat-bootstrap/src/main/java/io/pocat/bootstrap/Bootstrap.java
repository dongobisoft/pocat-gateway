package io.pocat.bootstrap;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

public class Bootstrap{
    private static final String POCAT_LIB_PATH_PROP_NAME = "io.pocat.libs";
    private static final String DEFAULT_LIB_PATH = System.getProperty("user.dir") + "./libs";

    public static void main(String[] args) throws Throwable {
        if(args.length < 1) {
            throw new IllegalArgumentException("Bootstrap class name does not exist.");
        }
        String libPath = System.getProperty(POCAT_LIB_PATH_PROP_NAME, DEFAULT_LIB_PATH);
        File libs = new File(libPath);
        File[] jars = libs.listFiles(pathname -> pathname.isFile() && pathname.getName().endsWith(".jar"));
        if(jars == null) {
            throw new IllegalArgumentException("Invalid library path [" + libs.getAbsolutePath() + "]");
        }
        ClassLoader libClassLoader = new URLClassLoader(Arrays.stream(jars).map(jarFile -> {
            try {
                return jarFile.toURI().toURL();
            } catch (MalformedURLException ignored) {

            }
            return null;
        }).toArray(URL[]::new));

        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(libClassLoader);
        try {
            Class<?> mainClazz = libClassLoader.loadClass(args[0]);
            Method mainMethod = mainClazz.getMethod("main", String[].class);
            mainMethod.invoke(null, (Object) Arrays.copyOfRange(args, 1, args.length));
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Class not found [" + args[0] + "]");
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Class [" + args[0] + "] does not have main method" );
        } catch (InvocationTargetException e) {
            throw e.getCause();
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Class [" + args[0] + "] access failed to main method" );
        }
        Thread.currentThread().setContextClassLoader(oldLoader);
    }
}
