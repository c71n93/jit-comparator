package comparator.method;

import comparator.property.Properties;
import comparator.property.PropertyString;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;

/**
 * Descriptor of the target method we want to investigate.
 */
public final class TargetMethod implements Properties {
    private static final PropertyString TARGET_CLASS = new PropertyString("comparator.jmh.targetClass");
    private static final PropertyString TARGET_METHOD = new PropertyString("comparator.jmh.targetMethod");

    private final Path classpath;
    private final Method method;

    public TargetMethod(final Path classpath, final String className, final String methodName) {
        this(classpath, TargetMethod.loadMethod(classpath, className, methodName));
    }

    public TargetMethod(final Path classpath, final Method method) {
        this.classpath = classpath;
        this.method = method;
    }

    public Path classpath() {
        return this.classpath;
    }

    public String className() {
        return this.method.getDeclaringClass().getName();
    }

    public String methodName() {
        return this.method.getName();
    }

    @Override
    public List<String> asJvmArgs() {
        return List.of(
                TargetMethod.TARGET_CLASS.asJvmArg(this.className()),
                TargetMethod.TARGET_METHOD.asJvmArg(this.methodName())
        );
    }

    public String classMethodName() {
        return this.className() + "::" + this.methodName();
    }

    public Method method() {
        return this.method;
    }

    @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
    public static Method runnableFromProperties() throws Exception { // TODO: implement exception handling
        final String className = TargetMethod.TARGET_CLASS.requireValue();
        final String methodName = TargetMethod.TARGET_METHOD.requireValue();
        final Class<?> clazz = Class.forName(className);
        final Method method = clazz.getDeclaredMethod(methodName);
        if (!Modifier.isStatic(method.getModifiers())) { // TODO: add possibility to run not only static methods
            throw new IllegalArgumentException("Minimal JMH run supports only static methods");
        }
        method.setAccessible(true);
        return method;
    }

    @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
    private static Method loadMethod(final Path classpath, final String className, final String methodName) {
        try (URLClassLoader loader = new URLClassLoader(
                new URL[]{classpath.toUri().toURL()}, Thread.currentThread().getContextClassLoader()
        )) {
            final Class<?> clazz = Class.forName(className, false, loader);
            final Method method = clazz.getDeclaredMethod(methodName);
            method.setAccessible(true);
            return method;
        } catch (final Exception e) {
            throw new IllegalStateException("Unable to load target method", e);
        }
    }
}
