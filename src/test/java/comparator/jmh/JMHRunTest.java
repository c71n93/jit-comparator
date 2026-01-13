package comparator.jmh;

import comparator.jmh.launch.JMHCommand;
import comparator.jmh.launch.JMHConfig;
import comparator.jmh.launch.JMHOutput;
import comparator.method.TargetMethod;
import comparator.jmh.fixtures.JMHTarget;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

final class JMHRunTest {
    @Test
    void returnsScoresFromJmhRun() {
        final Path classpath = Path.of("build/classes/java/test").toAbsolutePath();
        final TargetMethod target = new TargetMethod(
                classpath, JMHTarget.class.getName(), "succeed"
        );
        final JMHOutput output = new JMHCommand(target, new JMHConfig(true)).run();
        Assertions.assertTrue(Files.exists(output.jitlog()), "JMH run should produce JIT log file");
        final JMHResults results = output.results();
        Assertions.assertTrue(Double.isFinite(results.score().score()), "JMH run should produce primary score");
        Assertions
                .assertTrue(Double.isFinite(results.allocRateNorm().score()), "JMH run should produce alloc rate norm");
        Assertions.assertFalse(results.score().unit().isEmpty(), "Primary score unit should be present");
        Assertions.assertFalse(results.allocRateNorm().unit().isEmpty(), "Alloc rate norm unit should be present");
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
