package comparator.jmh.launch.benchmark;

import comparator.method.TargetMethod;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;

/**
 * The benchmark simply calls the reflection helper. JMH takes care of warmup
 * iterations, measurement and throttling for us.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class JMHBenchmark {
    /** Target method. */
    private static final Method TARGET_METHOD = TargetMethod.fromProperties().method();

    /**
     * Documented member.
     *
     * @return target method result
     */
    @Benchmark
    public final Object callTarget() throws IllegalAccessException, InvocationTargetException {
        return JMHBenchmark.TARGET_METHOD.invoke(null);
    }
}
