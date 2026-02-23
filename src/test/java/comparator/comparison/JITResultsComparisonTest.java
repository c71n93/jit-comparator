package comparator.comparison;

import comparator.Artifact;
import comparator.JITResults;
import comparator.jitlog.LogResults;
import comparator.jitlog.test.JITLogFixture;
import comparator.jmh.JMHAllocRateNorm;
import comparator.jmh.JMHInstructions;
import comparator.jmh.JMHMemoryLoads;
import comparator.jmh.JMHPrimaryScore;
import comparator.jmh.JMHResults;
import comparator.method.TargetMethod;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JITResultsComparisonTest {
    private static final String TARGET_CLASS = "comparator.jitlog.test.LogTarget";
    private static final String PRIMARY_SCORE_UNIT = "us/op";
    private static final String ALLOC_RATE_UNIT = "B";
    private static final String PERF_METRIC_UNIT = "#/op";
    private final JITLogFixture fixture = new JITLogFixture();

    @Test
    void returnsTrueWhenArtifactsWithinAccuracy(@TempDir final Path tempDir) throws Exception {
        final LogResults log = this.logResults(tempDir);
        final JITResults left = new JITResults(this.jmh(100.0d, 10.0d), log);
        final JITResults right = new JITResults(this.jmh(105.0d, 10.5d), log);
        Assertions.assertTrue(
                new JITResultsComparison(left, right).areSame(),
                "Equivalent metrics should be considered the same"
        );
    }

    @Test
    void returnsFalseWhenPrimaryScoreDiffers(@TempDir final Path tempDir) throws Exception {
        final LogResults log = this.logResults(tempDir);
        final JITResults left = new JITResults(this.jmh(100.0d, 10.0d), log);
        final JITResults right = new JITResults(this.jmh(120.0d, 10.0d), log);
        Assertions.assertFalse(
                new JITResultsComparison(left, right).areSame(),
                "Primary score difference should mark results as different"
        );
    }

    @Test
    void returnsFalseWhenAllocRateDiffers(@TempDir final Path tempDir) throws Exception {
        final LogResults log = this.logResults(tempDir);
        final JITResults left = new JITResults(this.jmh(100.0d, 10.0d), log);
        final JITResults right = new JITResults(this.jmh(105.0d, 12.0d), log);
        Assertions.assertFalse(
                new JITResultsComparison(left, right).areSame(),
                "Allocation rate difference should mark results as different"
        );
    }

    @Test
    void returnsFalseWhenInstructionsDiffer(@TempDir final Path tempDir) throws Exception {
        final LogResults log = this.logResults(tempDir);
        final JITResults left = new JITResults(this.jmhWithPerf(100.0d, 10.0d, 1000.0d, 2000.0d), log);
        final JITResults right = new JITResults(this.jmhWithPerf(105.0d, 10.5d, 1200.0d, 2100.0d), log);
        Assertions.assertFalse(
                new JITResultsComparison(left, right).areSame(),
                "Instructions difference should mark results as different"
        );
    }

    @Test
    void throwsWhenInstructionsPresenceDiffers(@TempDir final Path tempDir) throws Exception {
        final LogResults log = this.logResults(tempDir);
        final JITResults left = new JITResults(this.jmhWithPerf(100.0d, 10.0d, 1000.0d, 2000.0d), log);
        final JITResults right = new JITResults(this.jmh(100.0d, 10.0d), log);
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new JITResultsComparison(left, right).areSame(),
                "Different metric sets should not be compared"
        );
    }

    @Test
    void returnsFalseWhenMemoryLoadsDiffer(@TempDir final Path tempDir) throws Exception {
        final LogResults log = this.logResults(tempDir);
        final JITResults left = new JITResults(this.jmhWithPerf(100.0d, 10.0d, 1000.0d, 2000.0d), log);
        final JITResults right = new JITResults(this.jmhWithPerf(105.0d, 10.5d, 1005.0d, 2400.0d), log);
        Assertions.assertFalse(
                new JITResultsComparison(left, right).areSame(),
                "Memory loads difference should mark results as different"
        );
    }

    @Test
    void throwsWhenMemoryLoadsPresenceDiffers(@TempDir final Path tempDir) throws Exception {
        final LogResults log = this.logResults(tempDir);
        final JITResults left = new JITResults(this.jmhWithPerf(100.0d, 10.0d, 1000.0d, 2000.0d), log);
        final JITResults right = new JITResults(this.jmhWithInstructions(100.0d, 10.0d, 1000.0d), log);
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new JITResultsComparison(left, right).areSame(),
                "Different metric sets should not be compared"
        );
    }

    @Test
    void returnsZeroRelDiffForSameValues(@TempDir final Path tempDir) throws Exception {
        final LogResults log = this.logResults(tempDir);
        final JITResults left = new JITResults(this.jmh(100.0d, 10.0d), log);
        final JITResults right = new JITResults(this.jmh(100.0d, 10.0d), log);
        Assertions.assertEquals(
                0.0d,
                new JITResultsComparison(left, right).relativeDifference(),
                1.0e-12,
                "Same metrics should produce zero relative difference"
        );
    }

    @Test
    void calculatesRelDiffUsingMandatoryMetricsOnly(@TempDir final Path tempDir) throws Exception {
        final LogResults log = this.logResults(tempDir);
        final JITResults left = new JITResults(this.jmh(100.0d, 10.0d), log);
        final JITResults right = new JITResults(this.jmh(120.0d, 12.0d), log);
        final double expected = JITResultsComparisonTest.rms(
                JITResultsComparisonTest.artifactRelDiff(100.0d, 120.0d),
                JITResultsComparisonTest.artifactRelDiff(10.0d, 12.0d),
                0.0d
        );
        Assertions.assertEquals(
                expected,
                new JITResultsComparison(left, right).relativeDifference(),
                1.0e-12,
                "Mandatory metrics should be aggregated with RMS"
        );
    }

    @Test
    void calculatesRelDiffUsingAllMetricsWhenPerfPresent(@TempDir final Path tempDir) throws Exception {
        final LogResults log = this.logResults(tempDir);
        final JITResults left = new JITResults(this.jmhWithPerf(100.0d, 10.0d, 1000.0d, 2000.0d), log);
        final JITResults right = new JITResults(this.jmhWithPerf(120.0d, 12.0d, 1200.0d, 2400.0d), log);
        final double expected = JITResultsComparisonTest.rms(
                JITResultsComparisonTest.artifactRelDiff(100.0d, 120.0d),
                JITResultsComparisonTest.artifactRelDiff(10.0d, 12.0d),
                0.0d,
                JITResultsComparisonTest.artifactRelDiff(1000.0d, 1200.0d),
                JITResultsComparisonTest.artifactRelDiff(2000.0d, 2400.0d)
        );
        Assertions.assertEquals(
                expected,
                new JITResultsComparison(left, right).relativeDifference(),
                1.0e-12,
                "All present artifacts should participate in RMS aggregation"
        );
    }

    @Test
    void throwsWhenOptionalMetricsPresenceDiffersForRelDiff(@TempDir final Path tempDir) throws Exception {
        final LogResults log = this.logResults(tempDir);
        final JITResults left = new JITResults(this.jmhWithPerf(100.0d, 10.0d, 5000.0d, 9000.0d), log);
        final JITResults right = new JITResults(this.jmh(100.0d, 10.0d), log);
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new JITResultsComparison(left, right).relativeDifference(),
                "Different metric sets should not be compared"
        );
    }

    @Test
    void keepsRelDiffSymmetric(@TempDir final Path tempDir) throws Exception {
        final LogResults log = this.logResults(tempDir);
        final JITResults left = new JITResults(this.jmhWithPerf(100.0d, 10.0d, 1000.0d, 2000.0d), log);
        final JITResults right = new JITResults(this.jmhWithPerf(120.0d, 12.0d, 1200.0d, 2400.0d), log);
        Assertions.assertEquals(
                new JITResultsComparison(left, right).relativeDifference(),
                new JITResultsComparison(right, left).relativeDifference(),
                1.0e-12,
                "relDiff should be symmetric"
        );
    }

    @Test
    void returnsZeroForEmptyArtifactRows(@TempDir final Path tempDir) throws Exception {
        final LogResults log = this.logResults(tempDir);
        final JITResults left = new EmptyArtifactsJitResults(log);
        final JITResults right = new EmptyArtifactsJitResults(log);
        Assertions.assertEquals(
                0.0d,
                new JITResultsComparison(left, right).relativeDifference(),
                1.0e-12,
                "Empty rows should produce zero relative difference"
        );
    }

    private LogResults logResults(final Path tempDir) throws Exception {
        final Path logFile = tempDir.resolve("jit-log.xml");
        final TargetMethod target = new TargetMethod(
                this.testClasses(), JITResultsComparisonTest.TARGET_CLASS, "target"
        );
        this.fixture.generate(target, logFile);
        return new LogResults(target, logFile);
    }

    private JMHResults jmh(final double score, final double alloc) {
        return new JMHResults(
                new JMHPrimaryScore(score, JITResultsComparisonTest.PRIMARY_SCORE_UNIT),
                new JMHAllocRateNorm(alloc, JITResultsComparisonTest.ALLOC_RATE_UNIT)
        );
    }

    private JMHResults jmhWithInstructions(final double score, final double alloc, final double instructions) {
        return new JMHResults(
                new JMHPrimaryScore(score, JITResultsComparisonTest.PRIMARY_SCORE_UNIT),
                new JMHAllocRateNorm(alloc, JITResultsComparisonTest.ALLOC_RATE_UNIT),
                Optional.of(new JMHInstructions(instructions, JITResultsComparisonTest.PERF_METRIC_UNIT))
        );
    }

    private JMHResults jmhWithPerf(final double score, final double alloc, final double instructions,
            final double memoryLoads) {
        return new JMHResults(
                new JMHPrimaryScore(score, JITResultsComparisonTest.PRIMARY_SCORE_UNIT),
                new JMHAllocRateNorm(alloc, JITResultsComparisonTest.ALLOC_RATE_UNIT),
                Optional.of(new JMHInstructions(instructions, JITResultsComparisonTest.PERF_METRIC_UNIT)),
                Optional.of(new JMHMemoryLoads(memoryLoads, JITResultsComparisonTest.PERF_METRIC_UNIT))
        );
    }

    private Path testClasses() {
        return Path.of("build", "classes", "java", "test").toAbsolutePath();
    }

    private static double artifactRelDiff(final double left, final double right) {
        return 2.0d * Math.abs(left - right) / (Math.abs(left) + Math.abs(right) + Artifact.REL_DIFF_EPSILON);
    }

    private static double rms(final double... values) {
        double sumSquares = 0.0d;
        for (final double value : values) {
            sumSquares += value * value;
        }
        return Math.sqrt(sumSquares / values.length);
    }

    private static final class EmptyArtifactsJitResults extends JITResults {
        EmptyArtifactsJitResults(final LogResults log) {
            super(
                    new JMHResults(
                            new JMHPrimaryScore(0.0d, JITResultsComparisonTest.PRIMARY_SCORE_UNIT),
                            new JMHAllocRateNorm(0.0d, JITResultsComparisonTest.ALLOC_RATE_UNIT)
                    ),
                    log
            );
        }

        @Override
        public List<Artifact<?>> asArtifactRow() {
            return List.of();
        }
    }
}
