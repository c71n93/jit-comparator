package comparator.warmup.jmh;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * Small wrapper with a main method. The outer Java process launches this class
 * to execute the benchmarks inside a clean JVM that has JIT logging enabled.
 */
public final class WarmupEntryPoint {
    private WarmupEntryPoint() {
    }

    public static void main(final String[] args) throws Exception {
        final Options options = new OptionsBuilder()
                .include(WarmupBenchmark.class.getName())
                .warmupIterations(5) // TODO: figure out how to determine number of required warmup iterations
                .measurementIterations(1)
                .forks(0) // Running inside the already forked JVM.
                .shouldFailOnError(true)
                .build();
        new Runner(options).run();
    }
}
