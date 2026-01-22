package comparator.jmh;

import comparator.jmh.launch.JMHCommand;
import comparator.jmh.launch.JMHConfig;
import comparator.jmh.launch.JMHOutput;
import comparator.method.TargetMethod;
import comparator.jmh.fixtures.JMHTarget;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

final class JMHCommandTest {
    @Test
    void returnsScoresFromJmhRun() {
        final Path classpath = Path.of("build/classes/java/test").toAbsolutePath();
        final TargetMethod target = new TargetMethod(
                classpath, JMHTarget.class.getName(), "succeed"
        );
        final JMHOutput output = new JMHCommand(target, new JMHConfig(true)).run();
        Assertions.assertTrue(Files.exists(output.jitlog()), "JMH run should produce JIT log file");
        final JMHResults results = output.results();
        final List<String> row = results.asRow();
        Assertions.assertEquals(2, row.size(), "JMH row should contain two metrics");
        final double score = Double.parseDouble(row.get(0));
        final double allocRateNorm = Double.parseDouble(row.get(1));
        Assertions.assertTrue(Double.isFinite(score), "JMH run should produce primary score");
        Assertions.assertTrue(Double.isFinite(allocRateNorm), "JMH run should produce alloc rate norm");
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
                        () -> new JMHCommand(target, new JMHConfig(true)).run(),
                        "JMH run should fail on target exception"
                );
    }
}
