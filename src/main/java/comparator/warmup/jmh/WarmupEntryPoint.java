package comparator.warmup.jmh;

import comparator.warmup.WarmupConfig;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.openjdk.jmh.results.format.ResultFormatType;

/**
 * Small wrapper with a main method. The outer Java process launches this class
 * to execute the benchmarks inside a clean JVM that has JIT logging enabled.
 */
public final class WarmupEntryPoint {
    private static final long LOG_FLUSH_DELAY_MS = 500L;

    private WarmupEntryPoint() {
    }

    public static void main(final String[] args) throws Exception {
        final boolean quick = WarmupConfig.fromProperty().quick();
        final Options options = new OptionsBuilder()
                .include(WarmupBenchmark.class.getName())
                .warmupIterations(quick ? 1 : 5)
                .warmupTime(quick ? TimeValue.milliseconds(50) : TimeValue.seconds(3))
                .measurementIterations(1)
                .measurementTime(quick ? TimeValue.milliseconds(50) : TimeValue.seconds(1))
                .forks(0) // Running inside the already forked JVM.
                .addProfiler(GCProfiler.class)
                .shouldFailOnError(true)
                .result(JMHResultFile.resultFileFromProperty())
                .resultFormat(ResultFormatType.JSON)
                .build();
        new Runner(options).run();
        // TODO: This delay is needed to flush jit log. Find more elegant solution.
        TimeUnit.MILLISECONDS.sleep(WarmupEntryPoint.LOG_FLUSH_DELAY_MS);
    }
}
