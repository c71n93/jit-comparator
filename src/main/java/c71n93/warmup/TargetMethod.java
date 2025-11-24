package c71n93.warmup;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Simple descriptor of the method we want to warm up. To keep the example small
 * we only support parameterless static methods and require the caller to pass a
 * single classpath entry that contains the target bytecode.
 */
public final class TargetMethod {
    private static final String TARGET_CLASS = "c71n93.warmup.targetClass";
    private static final String TARGET_METHOD = "c71n93.warmup.targetMethod";

    private final Path classpath;
    private final String className;
    private final String methodName;

    public TargetMethod(final Path classpath, final String className, final String methodName) {
        this.classpath = classpath;
        this.className = className;
        this.methodName = methodName;
    }

    public Path classpath() {
        return classpath;
    }

    public String classProperty() {
        return "-D" + TargetMethod.TARGET_CLASS + "=" + this.className;
    }

    public String methodProperty() {
        return "-D" + TargetMethod.TARGET_METHOD + "=" + this.methodName;
    }

    public static Method runnableFromProperties() throws Exception { // TODO: implement exception handling
        final String className = requiredProperty(TargetMethod.TARGET_CLASS);
        final String methodName = requiredProperty(TargetMethod.TARGET_METHOD);
        final Class<?> clazz = Class.forName(className);
        final Method method = clazz.getDeclaredMethod(methodName);
        if (!Modifier.isStatic(method.getModifiers())) { // TODO: add possibility to run not only static methods
            throw new IllegalArgumentException("Minimal warmup supports only static methods");
        }
        method.setAccessible(true);
        return method;
    }

    private static String requiredProperty(final String name) {
        final String value = Objects.requireNonNull(System.getProperty(name), "Missing property: " + name);
        return value;
    }
}
