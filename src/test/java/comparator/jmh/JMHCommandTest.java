package comparator.jmh;

import comparator.jmh.launch.JMHCommand;
import comparator.jmh.launch.JMHConfig;
import comparator.jmh.launch.JMHOutput;
import comparator.jmh.launch.JMHResultFile;
import comparator.jmh.fixtures.JMHTarget;
import comparator.method.TargetMethod;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openjdk.jmh.runner.options.TimeValue;

final class JMHCommandTest {
    private static final String TEST_CLASSPATH = "build/classes/java/test";
    private static final String JIT_LOG_FILE_NAME = "jit-log.xml";

    @Test
    void exposesConfiguredArtifacts(@TempDir final Path tempDir) {
        final Path classpath = Path.of(JMHCommandTest.TEST_CLASSPATH).toAbsolutePath();
        final TargetMethod target = new TargetMethod(classpath, JMHTarget.class.getName(), "succeed");
        final Path jitlog = tempDir.resolve(JMHCommandTest.JIT_LOG_FILE_NAME);
        final JMHResultFile result = new JMHResultFile(tempDir.resolve("jmh-result.json"));
        final JMHCommand command = new JMHCommand(target, jitlog, result, JMHCommandTest.fastConfig(false));
        Assertions.assertSame(target, command.targetMethod(), "JMH command should expose configured target");
        Assertions.assertEquals(jitlog, command.jitlog(), "JMH command should expose configured JIT log");
        Assertions.assertSame(result, command.result(), "JMH command should expose configured result file");
    }

    @Test
    void returnsScoresFromJmhRun(@TempDir final Path tempDir) {
        final Path classpath = Path.of(JMHCommandTest.TEST_CLASSPATH).toAbsolutePath();
        final TargetMethod target = new TargetMethod(
                classpath, JMHTarget.class.getName(), "succeed"
        );
        final JMHOutput output = new JMHCommand(
                target,
                tempDir.resolve(JMHCommandTest.JIT_LOG_FILE_NAME),
                JMHCommandTest.fastConfig(false)
        ).run();
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
    void returnsScoresFromJMHPerfRun(@TempDir final Path tempDir) {
        Assumptions.assumeTrue(JMHCommandTest.perfAvailable(), "perf is required for this test");
        final Path classpath = Path.of(JMHCommandTest.TEST_CLASSPATH).toAbsolutePath();
        final TargetMethod target = new TargetMethod(
                classpath, JMHTarget.class.getName(), "succeed"
        );
        final JMHOutput output = new JMHCommand(
                target,
                tempDir.resolve(JMHCommandTest.JIT_LOG_FILE_NAME),
                JMHCommandTest.fastConfig(true)
        ).run();
        final JMHResults results = output.results();
        final List<String> row = results.asCsvRow();
        Assertions.assertEquals(5, row.size(), "JMH row should contain five metrics");
        Assertions.assertFalse(row.get(2).isEmpty(), "Instructions should be present when perf profiler is enabled");
        Assertions.assertFalse(row.get(3).isEmpty(), "Memory loads should be present when perf profiler is enabled");
        Assertions.assertFalse(row.get(4).isEmpty(), "Memory stores should be present when perf profiler is enabled");
    }

    @Test
    void throwsOnJmhFailure(@TempDir final Path tempDir) {
        final Path classpath = Path.of(JMHCommandTest.TEST_CLASSPATH).toAbsolutePath();
        final TargetMethod target = new TargetMethod(
                classpath, JMHTarget.class.getName(), "fail"
        );
        Assertions
                .assertThrows(
                        IllegalStateException.class,
                        () -> new JMHCommand(
                                target,
                                tempDir.resolve(JMHCommandTest.JIT_LOG_FILE_NAME),
                                JMHCommandTest.fastConfig(false)
                        ).run(),
                        "JMH run should fail on target exception"
                );
    }

    private static JMHConfig fastConfig(final boolean perfEnabled) {
        return new JMHConfig(1, TimeValue.milliseconds(50), 1, TimeValue.milliseconds(50), perfEnabled);
    }

    private static boolean perfAvailable() {
        try {
            final Process process = new ProcessBuilder(
                    "perf", "stat", "-e",
                    "instructions,mem_inst_retired.all_loads,mem_inst_retired.all_stores", "echo", "1"
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
