package comparator.jmh;

import comparator.jmh.launch.JMHCommand;
import comparator.jmh.launch.JMHConfig;
import comparator.jmh.launch.JMHOutput;
import comparator.jmh.fixtures.JMHTarget;
import comparator.method.TargetMethod;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.runner.options.TimeValue;

final class JMHCommandTest {
    @Test
    void returnsScoresFromJmhRun() {
        final Path classpath = Path.of("build/classes/java/test").toAbsolutePath();
        final TargetMethod target = new TargetMethod(
                classpath, JMHTarget.class.getName(), "succeed"
        );
        final JMHOutput output = new JMHCommand(target, JMHCommandTest.fastConfig(false)).run();
        Assertions.assertTrue(Files.exists(output.jitlog()), "JMH run should produce JIT log file");
        final JMHResults results = output.results();
        final List<String> row = results.asCsvRow();
        Assertions.assertEquals(2, row.size(), "JMH row should contain only mandatory metrics");
        final double score = Double.parseDouble(row.get(0));
        final double allocRateNorm = Double.parseDouble(row.get(1));
        Assertions.assertTrue(Double.isFinite(score), "JMH run should produce primary score");
        Assertions.assertTrue(Double.isFinite(allocRateNorm), "JMH run should produce alloc rate norm");
    }

    @Test
    void returnsScoresFromJMHPerfRun() {
        Assumptions.assumeTrue(JMHCommandTest.perfAvailable(), "perf is required for this test");
        final Path classpath = Path.of("build/classes/java/test").toAbsolutePath();
        final TargetMethod target = new TargetMethod(
                classpath, JMHTarget.class.getName(), "succeed"
        );
        final JMHOutput output = new JMHCommand(target, JMHCommandTest.fastConfig(true)).run();
        final JMHResults results = output.results();
        final List<String> row = results.asCsvRow();
        Assertions.assertEquals(4, row.size(), "JMH row should contain four metrics");
        Assertions.assertFalse(row.get(2).isEmpty(), "Instructions should be present when perf profiler is enabled");
        Assertions.assertFalse(row.get(3).isEmpty(), "Memory loads should be present when perf profiler is enabled");
    }

    @Test
    void throwsOnJmhFailure() {
        final Path classpath = Path.of("build/classes/java/test").toAbsolutePath();
        final TargetMethod target = new TargetMethod(
                classpath, JMHTarget.class.getName(), "fail"
        );
        Assertions
                .assertThrows(
                        IllegalStateException.class,
                        () -> new JMHCommand(target, JMHCommandTest.fastConfig(false)).run(),
                        "JMH run should fail on target exception"
                );
    }

    private static JMHConfig fastConfig(final boolean perfEnabled) {
        return new JMHConfig(1, TimeValue.milliseconds(50), 1, TimeValue.milliseconds(50), perfEnabled);
    }

    private static boolean perfAvailable() {
        try {
            final Process process = new ProcessBuilder(
                    "perf", "stat", "-e", "instructions,mem_inst_retired.all_loads", "echo", "1"
            ).start();
            process.getInputStream().readAllBytes();
            process.getErrorStream().readAllBytes();
            return process.waitFor() == 0;
        } catch (final InterruptedException exception) {
            Thread.currentThread().interrupt();
            return false;
        } catch (final IOException exception) {
            return false;
        }
    }
}
