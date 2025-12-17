package comparator.method;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.adoptopenjdk.jitwatch.model.MemberSignatureParts;

/**
 * Descriptor of the target method we want to investigate.
 */
public final class TargetMethod {
    private static final String TARGET_CLASS = "comparator.warmup.targetClass";
    private static final String TARGET_METHOD = "comparator.warmup.targetMethod";

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

    public String classProperty() {
        return "-D" + TargetMethod.TARGET_CLASS + "=" + this.className();
    }

    public String methodName() {
        return this.method.getName();
    }

    public String methodProperty() {
        return "-D" + TargetMethod.TARGET_METHOD + "=" + this.methodName();
    }

    public String classMethodName() {
        return this.className() + "::" + this.methodName();
    }

    public Method method() {
        return this.method;
    }

    @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
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
        return Objects.requireNonNull(System.getProperty(name), "Missing property: " + name);
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
