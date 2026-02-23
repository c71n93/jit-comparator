package comparator.jitlog;

import comparator.Artifact;
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
    private static final String LOG_FILE = "jit-log.xml";
    private static final String TARGET_METHOD = "target";
    private final JITLogFixture fixture = new JITLogFixture();

    @Test
    void returnsNativeSizeForTierFour(@TempDir final Path tempDir) throws Exception {
        final Path logFile = tempDir.resolve(NativeCodeSizeTest.LOG_FILE);
        final TargetMethod target = new TargetMethod(
                this.testClasses(),
                NativeCodeSizeTest.TARGET_CLASS,
                NativeCodeSizeTest.TARGET_METHOD
        );
        this.fixture.generate(target, logFile);
        final int size = new NativeCodeSize(target, logFile).value();
        Assertions.assertTrue(size > 0, "native code size should be positive");
    }

    @Test
    void comparesNativeCodeSizeUsingAccuracy(@TempDir final Path tempDir) throws Exception {
        final Path logFile = tempDir.resolve(NativeCodeSizeTest.LOG_FILE);
        final TargetMethod target = new TargetMethod(
                this.testClasses(),
                NativeCodeSizeTest.TARGET_CLASS,
                NativeCodeSizeTest.TARGET_METHOD
        );
        this.fixture.generate(target, logFile);
        final NativeCodeSize base = new NativeCodeSize(target, logFile);
        final NativeCodeSize same = new NativeCodeSize(target, logFile);
        final int size = base.value();
        final Artifact<Integer> other = new FixedIntArtifact(size + Math.max(1, size));
        Assertions.assertTrue(base.isSame(same), "Native code size should match for the same target");
        Assertions.assertFalse(base.isSame(other), "Native code size should not match when values differ");
        Assertions.assertFalse(other.isSame(base), "Native code size should not match when values differ");
    }

    @Test
    void returnsMinusOneWhenMethodAbsentInLog(@TempDir final Path tempDir) throws Exception {
        final Path logFile = tempDir.resolve(NativeCodeSizeTest.LOG_FILE);
        final TargetMethod target = new TargetMethod(
                this.testClasses(),
                NativeCodeSizeTest.TARGET_CLASS,
                NativeCodeSizeTest.TARGET_METHOD
        );
        this.fixture.generate(target, logFile);
        final TargetMethod missing = new TargetMethod(this.testClasses(), NativeCodeSizeTest.TARGET_CLASS, "absent");
        Assertions.assertEquals(
                -1, new NativeCodeSize(missing, logFile).value(), "Missing method in JIT log should return -1"
        );
    }

    @Test
    void returnsMinusOneWithoutTierFour(@TempDir final Path tempDir) throws Exception {
        final Path logFile = tempDir.resolve(NativeCodeSizeTest.LOG_FILE);
        final TargetMethod target = new TargetMethod(
                this.testClasses(),
                NativeCodeSizeTest.TARGET_CLASS,
                NativeCodeSizeTest.TARGET_METHOD
        );
        final List<String> flags = new ArrayList<>();
        flags.add("-XX:TieredStopAtLevel=1");
        this.fixture.generate(target, logFile, flags);
        Assertions.assertEquals(
                -1, new NativeCodeSize(target, logFile).value(), "No tier 4 compilation should return -1"
        );
    }

    private Path testClasses() {
        return Path.of("build", "classes", "java", "test").toAbsolutePath();
    }

    private static final class FixedIntArtifact implements Artifact<Integer> {
        private final Integer value;

        FixedIntArtifact(final int value) {
            this.value = value;
        }

        @Override
        public Integer value() {
            return this.value;
        }

        @Override
        public String headerCsv() {
            return "Fixed artifact";
        }
    }
}
