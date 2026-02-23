package comparator;

import comparator.jmh.fixtures.JMHTarget;
import comparator.jmh.launch.JMHConfig;
import comparator.method.TargetMethod;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openjdk.jmh.runner.options.TimeValue;

final class AnalysisTest {
    @Test
    void writesJitLogToProvidedPath(@TempDir final Path tempDir) {
        final Path classpath = Path.of("build", "classes", "java", "test").toAbsolutePath();
        final TargetMethod target = new TargetMethod(classpath, JMHTarget.class.getName(), "succeed");
        final Path jitlog = tempDir.resolve("provided-jit-log.xml");
        new Analysis(target, jitlog, AnalysisTest.fastConfig()).results();
        Assertions.assertTrue(Files.exists(jitlog), "Analysis should write JIT log to provided path");
    }

    private static JMHConfig fastConfig() {
        return new JMHConfig(1, TimeValue.milliseconds(50), 1, TimeValue.milliseconds(50), false);
    }
}
