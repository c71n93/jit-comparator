package comparator;

import comparator.jitlog.LogResults;
import comparator.jitlog.test.JITLogFixture;
import comparator.jmh.JMHAllocRateNorm;
import comparator.jmh.JMHPrimaryScore;
import comparator.jmh.JMHResults;
import comparator.method.TargetMethod;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JITResultsTest {
    private static final String TARGET_CLASS = "comparator.jitlog.test.LogTarget";
    private final JITLogFixture fixture = new JITLogFixture();

    @Test
    void returnsTrueWhenArtifactsWithinAccuracy(@TempDir final Path tempDir) throws Exception {
        final LogResults log = this.logResults(tempDir);
        final JITResults left = new JITResults(this.jmh(100.0d, 10.0d), log);
        final JITResults right = new JITResults(this.jmh(105.0d, 10.5d), log);
        Assertions.assertTrue(left.isSame(right), "Equivalent metrics should be considered the same");
    }

    @Test
    void returnsFalseWhenPrimaryScoreDiffers(@TempDir final Path tempDir) throws Exception {
        final LogResults log = this.logResults(tempDir);
        final JITResults left = new JITResults(this.jmh(100.0d, 10.0d), log);
        final JITResults right = new JITResults(this.jmh(120.0d, 10.0d), log);
        Assertions.assertFalse(left.isSame(right), "Primary score difference should mark results as different");
    }

    @Test
    void returnsFalseWhenAllocRateDiffers(@TempDir final Path tempDir) throws Exception {
        final LogResults log = this.logResults(tempDir);
        final JITResults left = new JITResults(this.jmh(100.0d, 10.0d), log);
        final JITResults right = new JITResults(this.jmh(105.0d, 12.0d), log);
        Assertions.assertFalse(left.isSame(right), "Allocation rate difference should mark results as different");
    }

    private LogResults logResults(final Path tempDir) throws Exception {
        final Path logFile = tempDir.resolve("jit-log.xml");
        final TargetMethod target = new TargetMethod(this.testClasses(), JITResultsTest.TARGET_CLASS, "target");
        this.fixture.generate(target, logFile);
        return new LogResults(target, logFile);
    }

    private JMHResults jmh(final double score, final double alloc) {
        return new JMHResults(new JMHPrimaryScore(score, "us/op"), new JMHAllocRateNorm(alloc, "B"));
    }

    private Path testClasses() {
        return Path.of("build", "classes", "java", "test").toAbsolutePath();
    }
}
