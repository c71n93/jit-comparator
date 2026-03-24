package comparator.jmh;

import comparator.jmh.launch.JMHCommand;
import comparator.jmh.launch.JMHConfig;
import comparator.jmh.launch.JMHOutput;
import comparator.jmh.fixtures.JMHTarget;
import comparator.jmh.perf.PerfMemoryEvents;
import comparator.method.TargetMethod;
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
    private static final String TARGET_METHOD = "succeed";

    @Test
    void exposesConfiguredArtifacts(@TempDir final Path tempDir) {
        final Path classpath = Path.of(JMHCommandTest.TEST_CLASSPATH).toAbsolutePath();
        final TargetMethod target = new TargetMethod(classpath, JMHTarget.class.getName(), JMHCommandTest.TARGET_METHOD);
        final Path jitlog = tempDir.resolve(JMHCommandTest.JIT_LOG_FILE_NAME);
        final Path result = tempDir.resolve("jmh-result.json");
        final JMHCommand command = new JMHCommand(target, jitlog, result, JMHCommandTest.fastConfig(false));
        Assertions.assertSame(target, command.targetMethod(), "JMH command should expose configured target");
        Assertions.assertEquals(jitlog, command.jitlog(), "JMH command should expose configured JIT log");
        Assertions.assertEquals(
                result.toString(), command.result().toString(), "JMH command should expose configured result file"
        );
    }

    @Test
    void keepsDefaultArtifactsNearTargetClassWithoutPrecreatingFiles() {
        final Path classpath = Path.of(JMHCommandTest.TEST_CLASSPATH).toAbsolutePath();
        final TargetMethod target = new TargetMethod(classpath, JMHTarget.class.getName(), JMHCommandTest.TARGET_METHOD);
        final JMHCommand command = new JMHCommand(target, JMHCommandTest.fastConfig(false));
        final Path expected = classpath.resolve(JMHTarget.class.getName().replace('.', '/')).getParent();
        final Path result = Path.of(command.result().toString());
        Assertions.assertEquals(expected, command.jitlog().getParent(), "Default JIT log should be near target class");
        Assertions.assertEquals(expected, result.getParent(), "Default JMH result should be near target class");
        Assertions.assertTrue(
                command.jitlog().getFileName().toString().startsWith(JMHTarget.class.getSimpleName() + "-jit-log-"),
                "Default JIT log should include target class name"
        );
        Assertions.assertTrue(
                result.getFileName().toString().startsWith(JMHTarget.class.getSimpleName() + "-jmh-result-"),
                "Default JMH result should include target class name"
        );
        Assertions.assertFalse(Files.exists(command.jitlog()), "Default JIT log should not be created before JMH runs");
        Assertions.assertFalse(Files.exists(result), "Default JMH result should not be created before JMH runs");
    }

    @Test
    void returnsScoresFromJmhRun(@TempDir final Path tempDir) {
        final Path classpath = Path.of(JMHCommandTest.TEST_CLASSPATH).toAbsolutePath();
        final TargetMethod target = new TargetMethod(
                classpath, JMHTarget.class.getName(), JMHCommandTest.TARGET_METHOD
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
        Assumptions.assumeTrue(PerfMemoryEvents.memEventsAvailable(), "perf memory events are required for this test");
        final Path classpath = Path.of(JMHCommandTest.TEST_CLASSPATH).toAbsolutePath();
        final TargetMethod target = new TargetMethod(
                classpath, JMHTarget.class.getName(), JMHCommandTest.TARGET_METHOD
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
}
