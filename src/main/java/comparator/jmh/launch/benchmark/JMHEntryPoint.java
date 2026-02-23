package comparator.jmh.launch.benchmark;

import comparator.jmh.launch.JMHConfig;
import comparator.jmh.launch.JMHJitLogFile;
import comparator.jmh.launch.JMHResultFile;
import comparator.method.TargetMethod;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.profile.LinuxPerfNormProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.results.format.ResultFormatType;

/**
 * Small wrapper with a main method. The outer Java process launches this class
 * to execute the benchmarks inside a clean JVM that has JIT logging enabled.
 */
public final class JMHEntryPoint {
    private static final long LOG_FLUSH_DELAY_MS = 500L;

    private JMHEntryPoint() {
    }

    public static void main(final String[] args) throws Exception {
        final JMHConfig config = JMHConfig.fromProperties();
        final var builder = new OptionsBuilder()
                .include(JMHBenchmark.class.getName())
                .warmupIterations(config.warmupIterations())
                .warmupTime(config.warmupTime())
                .measurementIterations(config.measurementIterations())
                .measurementTime(config.measurementTime())
                .forks(1) // TODO: Add possibility to configure number of forks via JMHConfig
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
            builder.addProfiler(
                    LinuxPerfNormProfiler.class,
                    "events=instructions,mem_inst_retired.all_loads,mem_inst_retired.all_stores"
            );
        }
        final Options options = builder.build();
        new Runner(options).run();
        // TODO: This delay is needed to flush jit log. Find more elegant solution.
        TimeUnit.MILLISECONDS.sleep(JMHEntryPoint.LOG_FLUSH_DELAY_MS);
    }
}
