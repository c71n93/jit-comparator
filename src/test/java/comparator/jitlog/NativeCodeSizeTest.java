package comparator.jitlog;

import comparator.jitlog.test.JITLogFixture;
import comparator.method.TargetMethod;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class NativeCodeSizeTest {
    private static final String TARGET_CLASS = "comparator.jitlog.test.LogTarget";
    private final JITLogFixture fixture = new JITLogFixture();

    @Test
    void returnsNativeSizeForTierFour(@TempDir final Path tempDir) throws Exception {
        final Path logFile = tempDir.resolve("jit-log.xml");
        final TargetMethod target = new TargetMethod(this.testClasses(), NativeCodeSizeTest.TARGET_CLASS, "target");
        this.fixture.generate(target, logFile);
        final int size = new NativeCodeSize(target, logFile).value();
        Assertions.assertTrue(size > 0, "native code size should be positive");
    }

    @Test
    void failsWhenMethodAbsentInLog(@TempDir final Path tempDir) throws Exception {
        final Path logFile = tempDir.resolve("jit-log.xml");
        final TargetMethod target = new TargetMethod(this.testClasses(), NativeCodeSizeTest.TARGET_CLASS, "target");
        this.fixture.generate(target, logFile);
        final TargetMethod missing = new TargetMethod(this.testClasses(), NativeCodeSizeTest.TARGET_CLASS, "absent");
        Assertions.assertThrows(IllegalStateException.class, () -> new NativeCodeSize(missing, logFile).value());
    }

    @Test
    void failsWithoutTierFour(@TempDir final Path tempDir) throws Exception {
        final Path logFile = tempDir.resolve("jit-log.xml");
        final TargetMethod target = new TargetMethod(this.testClasses(), NativeCodeSizeTest.TARGET_CLASS, "target");
        final List<String> flags = new ArrayList<>();
        flags.add("-XX:TieredStopAtLevel=1");
        this.fixture.generate(target, logFile, flags);
        Assertions.assertThrows(IllegalStateException.class, () -> new NativeCodeSize(target, logFile).value());
    }

    private Path testClasses() {
        return Path.of("build", "classes", "java", "test").toAbsolutePath();
    }
}
