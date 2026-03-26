package comparator.jmh;

import comparator.Artifact;
import comparator.Metric;
import comparator.jmh.results.JMHAllocRateNorm;
import comparator.jmh.results.JMHAllocRateNormError;
import comparator.jmh.results.JMHAllocRateNormResults;
import comparator.jmh.results.JMHInstructions;
import comparator.jmh.results.JMHMemoryLoads;
import comparator.jmh.results.JMHMemoryStores;
import comparator.jmh.results.JMHPrimaryScore;
import comparator.jmh.results.JMHPrimaryScoreError;
import comparator.jmh.results.JMHPrimaryScoreResults;
import comparator.jmh.results.JMHResults;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JMHResultsTest {
    private static final String PRIMARY_SCORE_UNIT = "us/op";
    private static final String ALLOC_RATE_UNIT = "B/op";
    private static final String PERF_METRIC_UNIT = "#/op";
    private static final String MISSING_ERROR = "NaN";

    @Test
    void exposesMetricsAsCsvRow() {
        final JMHResults results = new JMHResults(
                new JMHPrimaryScore(1.5, JMHResultsTest.PRIMARY_SCORE_UNIT),
                new JMHAllocRateNorm(2.5, JMHResultsTest.ALLOC_RATE_UNIT),
                new JMHInstructions(3.5, JMHResultsTest.PERF_METRIC_UNIT),
                new JMHMemoryLoads(4.5, JMHResultsTest.PERF_METRIC_UNIT),
                new JMHMemoryStores(5.5, JMHResultsTest.PERF_METRIC_UNIT)
        );
        Assertions.assertEquals(
                List.of("1.5", JMHResultsTest.MISSING_ERROR, "2.5", JMHResultsTest.MISSING_ERROR, "3.5", "4.5", "5.5"),
                results.asCsvRow(),
                "JMH results should expose metric values"
        );
    }

    @Test
    void exposesMetricsAsArtifactRow() {
        final JMHResults results = new JMHResults(
                new JMHPrimaryScore(1.5, JMHResultsTest.PRIMARY_SCORE_UNIT),
                new JMHAllocRateNorm(2.5, JMHResultsTest.ALLOC_RATE_UNIT),
                new JMHInstructions(3.5, JMHResultsTest.PERF_METRIC_UNIT),
                new JMHMemoryLoads(4.5, JMHResultsTest.PERF_METRIC_UNIT),
                new JMHMemoryStores(5.5, JMHResultsTest.PERF_METRIC_UNIT)
        );
        final List<Artifact<?>> artifacts = results.asArtifactRow();
        Assertions.assertEquals(7, artifacts.size(), "JMH artifact row should contain metrics and relative errors");
        Assertions.assertInstanceOf(JMHPrimaryScore.class, artifacts.get(0), "Primary score should be first");
        Assertions
                .assertInstanceOf(JMHPrimaryScoreError.class, artifacts.get(1), "Primary score error should be second");
        Assertions.assertInstanceOf(JMHAllocRateNorm.class, artifacts.get(2), "Alloc rate norm should be third");
        Assertions.assertInstanceOf(
                JMHAllocRateNormError.class,
                artifacts.get(3),
                "Alloc rate norm error should be fourth"
        );
        Assertions.assertInstanceOf(JMHInstructions.class, artifacts.get(4), "Instructions should be fifth");
        Assertions.assertInstanceOf(JMHMemoryLoads.class, artifacts.get(5), "Memory loads should be sixth");
        Assertions.assertInstanceOf(JMHMemoryStores.class, artifacts.get(6), "Memory stores should be seventh");
        Assertions.assertEquals(
                1.5d, artifacts.get(0).value().doubleValue(), 1.0e-12, "Primary score value should match"
        );
        Assertions.assertTrue(
                Double.isNaN(artifacts.get(1).value().doubleValue()), "Primary error should default to NaN"
        );
        Assertions.assertEquals(2.5d, artifacts.get(2).value().doubleValue(), 1.0e-12, "Alloc rate value should match");
        Assertions
                .assertTrue(Double.isNaN(artifacts.get(3).value().doubleValue()), "Alloc error should default to NaN");
        Assertions
                .assertEquals(3.5d, artifacts.get(4).value().doubleValue(), 1.0e-12, "Instructions value should match");
        Assertions
                .assertEquals(4.5d, artifacts.get(5).value().doubleValue(), 1.0e-12, "Memory loads value should match");
        Assertions
                .assertEquals(
                        5.5d, artifacts.get(6).value().doubleValue(), 1.0e-12, "Memory stores value should match"
                );
    }

    @Test
    void exposesMetricsWithoutMemoryEventsAsCsvRow() {
        final JMHResults results = new JMHResults(
                new JMHPrimaryScore(1.5, JMHResultsTest.PRIMARY_SCORE_UNIT),
                new JMHAllocRateNorm(2.5, JMHResultsTest.ALLOC_RATE_UNIT),
                new JMHInstructions(3.5, JMHResultsTest.PERF_METRIC_UNIT)
        );
        Assertions.assertEquals(
                List.of("1.5", JMHResultsTest.MISSING_ERROR, "2.5", JMHResultsTest.MISSING_ERROR, "3.5"),
                results.asCsvRow(),
                "Instructions-only perf state should expose metrics and relative errors"
        );
    }

    @Test
    void exposesMetricsWithoutMemoryEventsAsArtifactRow() {
        final JMHResults results = new JMHResults(
                new JMHPrimaryScore(1.5, JMHResultsTest.PRIMARY_SCORE_UNIT),
                new JMHAllocRateNorm(2.5, JMHResultsTest.ALLOC_RATE_UNIT),
                new JMHInstructions(3.5, JMHResultsTest.PERF_METRIC_UNIT)
        );
        final List<Artifact<?>> artifacts = results.asArtifactRow();
        Assertions.assertEquals(5, artifacts.size(), "Instructions-only perf state should expose five artifacts");
        Assertions.assertInstanceOf(JMHPrimaryScore.class, artifacts.get(0), "Primary score should be first");
        Assertions
                .assertInstanceOf(JMHPrimaryScoreError.class, artifacts.get(1), "Primary score error should be second");
        Assertions.assertInstanceOf(JMHAllocRateNorm.class, artifacts.get(2), "Alloc rate norm should be third");
        Assertions.assertInstanceOf(
                JMHAllocRateNormError.class,
                artifacts.get(3),
                "Alloc rate norm error should be fourth"
        );
        Assertions.assertInstanceOf(JMHInstructions.class, artifacts.get(4), "Instructions should be fifth");
    }

    @Test
    void omitsPerfMetricsInCsvRowWhenMissing() {
        final JMHResults results = new JMHResults(
                new JMHPrimaryScore(1.5, JMHResultsTest.PRIMARY_SCORE_UNIT),
                new JMHAllocRateNorm(2.5, JMHResultsTest.ALLOC_RATE_UNIT)
        );
        Assertions.assertEquals(
                List.of("1.5", JMHResultsTest.MISSING_ERROR, "2.5", JMHResultsTest.MISSING_ERROR),
                results.asCsvRow(),
                "Missing perf metrics should be omitted from csv row"
        );
    }

    @Test
    void omitsPerfMetricsInArtifactRowWhenMissing() {
        final JMHResults results = new JMHResults(
                new JMHPrimaryScore(1.5, JMHResultsTest.PRIMARY_SCORE_UNIT),
                new JMHAllocRateNorm(2.5, JMHResultsTest.ALLOC_RATE_UNIT)
        );
        final List<Artifact<?>> artifacts = results.asArtifactRow();
        Assertions.assertEquals(4, artifacts.size(), "Missing perf metrics should be omitted from artifact row");
        Assertions.assertInstanceOf(JMHPrimaryScore.class, artifacts.get(0), "Primary score should be first");
        Assertions
                .assertInstanceOf(JMHPrimaryScoreError.class, artifacts.get(1), "Primary score error should be second");
        Assertions.assertInstanceOf(JMHAllocRateNorm.class, artifacts.get(2), "Alloc rate norm should be third");
        Assertions.assertInstanceOf(
                JMHAllocRateNormError.class,
                artifacts.get(3),
                "Alloc rate norm error should be fourth"
        );
    }

    @Test
    void excludesRelativeErrorsFromMetricRow() {
        final JMHResults results = new JMHResults(
                new JMHPrimaryScoreResults(
                        new JMHPrimaryScore(1.5, JMHResultsTest.PRIMARY_SCORE_UNIT),
                        new JMHPrimaryScoreError(0.1d)
                ),
                new JMHAllocRateNormResults(
                        new JMHAllocRateNorm(2.5, JMHResultsTest.ALLOC_RATE_UNIT),
                        new JMHAllocRateNormError(0.2d)
                ),
                new JMHInstructions(3.5, JMHResultsTest.PERF_METRIC_UNIT)
        );
        final List<Metric<?>> metrics = results.asMetricRow();
        Assertions.assertEquals(3, metrics.size(), "Relative errors should not participate in metric comparison rows");
        Assertions.assertInstanceOf(JMHPrimaryScore.class, metrics.get(0), "Primary score should stay in metric row");
        Assertions.assertInstanceOf(JMHAllocRateNorm.class, metrics.get(1), "Alloc rate should stay in metric row");
        Assertions.assertInstanceOf(JMHInstructions.class, metrics.get(2), "Perf metric should stay in metric row");
    }
}
