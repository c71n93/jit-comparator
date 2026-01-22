package comparator.method;

import comparator.jmh.fixtures.JMHTarget;
import comparator.method.fixtures.InstanceTarget;
import java.lang.reflect.Method;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TargetMethodTest {
    @Test
    void loadsTargetMethodFromProperties() throws Exception {
        final String classKey = "comparator.jmh.targetClass";
        final String methodKey = "comparator.jmh.targetMethod";
        final String previousClass = System.getProperty(classKey);
        final String previousMethod = System.getProperty(methodKey);
        System.setProperty(classKey, JMHTarget.class.getName());
        System.setProperty(methodKey, "succeed");
        try {
            final Method method = TargetMethod.fromProperties().method();
            final Object result = method.invoke(null);
            Assertions.assertEquals("ok", result, "Target method should be loaded from system properties");
        } finally {
            this.restoreProperty(classKey, previousClass);
            this.restoreProperty(methodKey, previousMethod);
        }
    }

    @Test
    void rejectsNonStaticMethods() {
        final Path classpath = Path.of("build", "classes", "java", "test").toAbsolutePath();
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new TargetMethod(classpath, InstanceTarget.class.getName(), "run"),
                "Target method should be static"
        );
    }

    private void restoreProperty(final String key, final String value) {
        if (value == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, value);
        }
    }
}
