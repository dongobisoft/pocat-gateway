package io.pocat.utils.clazz;

import java.util.ArrayList;
import java.util.List;

public class ClassHelper {
    public static Object createInstance(String clazzName, Object... args) throws InstantiationFailedException {
        try {
            Class<?> clazz = Class.forName(clazzName);
            List<Class<?>> argsTypes = new ArrayList<>();
            for(Object arg:args) {
                argsTypes.add(arg.getClass());
            }
            return clazz.getConstructor(argsTypes.toArray(new Class<?>[0])).newInstance(args);
        } catch (Exception e) {
            throw new InstantiationFailedException("Class [" + clazzName + "] initialization failed.", e);
        }

    }
}
