package comparator.jmh.launch.benchmark;

import comparator.jmh.launch.JMHConfig;
import comparator.jmh.launch.output.JMHJitLogFile;
import comparator.jmh.launch.output.JMHResultFile;
import comparator.jmh.launch.output.perf.PerfMemoryEvents;
import comparator.method.TargetMethod;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.profile.LinuxPerfNormProfiler;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * Small wrapper with a main method. The outer Java process launches this class
 * to execute the benchmarks inside a clean JVM that has JIT logging enabled.
 */
public final class JMHEntryPoint {
    /** Instructions event. */
    private static final String INSTRUCTIONS_EVENT = "instructions";

    private JMHEntryPoint() {
    }

    /**
     * main.
     *
     * @param args command line arguments
     */
    public static void main(final String[] args) throws RunnerException {
        final JMHConfig config = JMHConfig.fromProperties();
        final ChainedOptionsBuilder builder = new OptionsBuilder()
            .include(JMHBenchmark.class.getName())
            .warmupIterations(config.warmupIterations())
            .warmupTime(config.warmupTime())
            .measurementIterations(config.measurementIterations())
            .measurementTime(config.measurementTime())
            // @todo #14:30min Add possibility to configure number of forks via JMHConfig.
            .forks(1)
            .jvmArgsAppend(
                "-XX:CompileCommand=print," + TargetMethod.fromProperties().classMethodName(),
                "-XX:+UnlockDiagnosticVMOptions",
                "-XX:+LogCompilation",
                "-XX:LogFile=" + JMHJitLogFile.fileFromProperty().toAbsolutePath()
            )
            .addProfiler(GCProfiler.class)
            .shouldFailOnError(true)
            .result(JMHResultFile.resultFileFromProperty())
            .resultFormat(ResultFormatType.JSON);
        if (config.perfEnabled()) {
            final String memEvents = PerfMemoryEvents.events().eventNames();
            final String events = memEvents.isEmpty()
                ? "events=" + JMHEntryPoint.INSTRUCTIONS_EVENT
                : "events=" + JMHEntryPoint.INSTRUCTIONS_EVENT + "," + memEvents;
            builder.addProfiler(
                LinuxPerfNormProfiler.class,
                events
            );
        }
        final Options options = builder.build();
        new Runner(options).run();
    }
}
