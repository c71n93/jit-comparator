package comparator.method;

import comparator.property.JvmSystemProperties;
import comparator.property.PropertyString;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;

/**
 * Descriptor of a target method along with the classpath needed to load it.
 */
public final class TargetMethod implements JvmSystemProperties {
    private static final PropertyString TARGET_CLASS = new PropertyString("comparator.jmh.targetClass");
    private static final PropertyString TARGET_METHOD = new PropertyString("comparator.jmh.targetMethod");

    private final Classpath classpath;
    private final Method method;

    /**
     * Ctor.
     *
     * @param classpath
     *            classpath that contains the target class
     * @param className
     *            fully qualified target class name
     * @param methodName
     *            target method name
     */
    public TargetMethod(final Path classpath, final String className, final String methodName) {
        this(new Classpath(classpath), className, methodName);
    }

    /**
     * Ctor.
     *
     * @param classpath
     *            classpath that contains the target class
     * @param className
     *            fully qualified target class name
     * @param methodName
     *            target method name
     */
    public TargetMethod(final Classpath classpath, final String className, final String methodName) {
        this(classpath, TargetMethod.loadMethod(classpath, className, methodName));
    }

    /**
     * Ctor.
     *
     * @param classpath
     *            classpath that contains the target class
     * @param method
     *            resolved target method
     */
    public TargetMethod(final Path classpath, final Method method) {
        this(new Classpath(classpath), method);
    }

    /**
     * Ctor.
     *
     * @param classpath
     *            classpath that contains the target class
     * @param method
     *            resolved target method
     */
    public TargetMethod(final Classpath classpath, final Method method) {
        this.classpath = classpath;
        this.method = method;
    }

    /**
     * @return classpath entries
     */
    public Classpath classpath() {
        return this.classpath;
    }

    /**
     * @return target method
     */
    public Method method() {
        return this.method;
    }

    /**
     * @return target class name
     */
    public String className() {
        return this.method.getDeclaringClass().getName();
    }

    /**
     * @return target method name
     */
    public String methodName() {
        return this.method.getName();
    }

    /**
     * Returns a human-readable {@code ClassName::methodName} identifier.
     *
     * @return combined class and method name
     */
    public String classMethodName() {
        return this.className() + "::" + this.methodName();
    }

    @Override
    public List<String> asJvmPropertyArgs() {
        return List.of(
                TargetMethod.TARGET_CLASS.asJvmArg(this.className()),
                TargetMethod.TARGET_METHOD.asJvmArg(this.methodName())
        );
    }

    /**
     * Creates itself from given system properties using reflection.
     *
     * @return reflected target method
     */
    @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
    public static TargetMethod fromProperties() {
        return new TargetMethod(
                new Classpath(new PropertyString("java.class.path").requireValue()),
                TargetMethod.TARGET_CLASS.requireValue(),
                TargetMethod.TARGET_METHOD.requireValue()
        );
    }

    /**
     * Loads the method using a dedicated classloader for the provided classpath.
     *
     * @param classpath
     *            classpath that contains the target class
     * @param className
     *            fully qualified target class name
     * @param methodName
     *            target method name
     * @return resolved method
     */
    @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
    private static Method loadMethod(final Classpath classpath, final String className, final String methodName) {
        try (URLClassLoader loader = new URLClassLoader(
                classpath.urls().toArray(new URL[0]), Thread.currentThread().getContextClassLoader()
        )) {
            final Class<?> clazz = Class.forName(className, false, loader);
            final Method method = clazz.getDeclaredMethod(methodName);
            if (!Modifier.isStatic(method.getModifiers())) {
                throw new IllegalArgumentException("Non static methods are not supported");
            }
            method.setAccessible(true);
            return method;
        } catch (final IOException | ClassNotFoundException | NoSuchMethodException e) {
            throw new IllegalStateException("Unable to load target method", e);
        }
    }
}
