package comparator.warmup.jmh;

import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;

/**
 * The benchmark simply calls the reflection helper. JMH takes care of warmup
 * loops, measurement and throttling for us.
 */
@BenchmarkMode(Mode.AverageTime) // TODO: To decide which mode is best suited for our purposes.
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class WarmupBenchmark {

    @Benchmark
    public Object callTarget(final WarmupTargetState state) throws Exception {
        return state.invoke();
    }
}
