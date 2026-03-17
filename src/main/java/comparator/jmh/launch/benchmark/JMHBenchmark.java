package comparator.jmh.launch.benchmark;

import comparator.method.TargetMethod;
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
@BenchmarkMode(Mode.AverageTime) // TODO: To decide which mode is best suited for our purposes.
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class JMHBenchmark {
    private static final Method targetMethod = TargetMethod.fromProperties().method();

    @Benchmark
    public Object callTarget() throws Exception {
        return JMHBenchmark.targetMethod.invoke(null);
    }
}
