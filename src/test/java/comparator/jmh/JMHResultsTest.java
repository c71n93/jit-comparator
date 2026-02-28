package comparator.jmh;

import comparator.Artifact;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JMHResultsTest {
    private static final String PRIMARY_SCORE_UNIT = "us/op";
    private static final String ALLOC_RATE_UNIT = "B/op";
    private static final String PERF_METRIC_UNIT = "#/op";

    @Test
    void exposesMetricsAsCsvRow() {
        final JMHResults results = new JMHResults(
                new JMHPrimaryScore(1.5, JMHResultsTest.PRIMARY_SCORE_UNIT),
                new JMHAllocRateNorm(2.5, JMHResultsTest.ALLOC_RATE_UNIT),
                Optional.of(new JMHInstructions(3.5, JMHResultsTest.PERF_METRIC_UNIT)),
                Optional.of(new JMHMemoryLoads(4.5, JMHResultsTest.PERF_METRIC_UNIT)),
                Optional.of(new JMHMemoryStores(5.5, JMHResultsTest.PERF_METRIC_UNIT))
        );
        Assertions.assertEquals(
                List.of("1.5", "2.5", "3.5", "4.5", "5.5"),
                results.asCsvRow(),
                "JMH results should expose metric values"
        );
    }

    @Test
    void exposesMetricsAsArtifactRow() {
        final JMHResults results = new JMHResults(
                new JMHPrimaryScore(1.5, JMHResultsTest.PRIMARY_SCORE_UNIT),
                new JMHAllocRateNorm(2.5, JMHResultsTest.ALLOC_RATE_UNIT),
                Optional.of(new JMHInstructions(3.5, JMHResultsTest.PERF_METRIC_UNIT)),
                Optional.of(new JMHMemoryLoads(4.5, JMHResultsTest.PERF_METRIC_UNIT)),
                Optional.of(new JMHMemoryStores(5.5, JMHResultsTest.PERF_METRIC_UNIT))
        );
        final List<Artifact<?>> artifacts = results.asArtifactRow();
        Assertions.assertEquals(5, artifacts.size(), "JMH artifact row should contain five metrics");
        Assertions.assertInstanceOf(JMHPrimaryScore.class, artifacts.get(0), "Primary score should be first");
        Assertions.assertInstanceOf(JMHAllocRateNorm.class, artifacts.get(1), "Alloc rate norm should be second");
        Assertions.assertInstanceOf(JMHInstructions.class, artifacts.get(2), "Instructions should be third");
        Assertions.assertInstanceOf(JMHMemoryLoads.class, artifacts.get(3), "Memory loads should be fourth");
        Assertions.assertInstanceOf(JMHMemoryStores.class, artifacts.get(4), "Memory stores should be fifth");
        Assertions.assertEquals(
                1.5d, artifacts.get(0).value().doubleValue(), 1.0e-12, "Primary score value should match"
        );
        Assertions.assertEquals(2.5d, artifacts.get(1).value().doubleValue(), 1.0e-12, "Alloc rate value should match");
        Assertions
                .assertEquals(3.5d, artifacts.get(2).value().doubleValue(), 1.0e-12, "Instructions value should match");
        Assertions
                .assertEquals(4.5d, artifacts.get(3).value().doubleValue(), 1.0e-12, "Memory loads value should match");
        Assertions
                .assertEquals(
                        5.5d, artifacts.get(4).value().doubleValue(), 1.0e-12, "Memory stores value should match"
                );
    }

    @Test
    void omitsPerfMetricsInCsvRowWhenMissing() {
        final JMHResults results = new JMHResults(
                new JMHPrimaryScore(1.5, JMHResultsTest.PRIMARY_SCORE_UNIT),
                new JMHAllocRateNorm(2.5, JMHResultsTest.ALLOC_RATE_UNIT)
        );
        Assertions.assertEquals(
                List.of("1.5", "2.5"),
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
        Assertions.assertEquals(2, artifacts.size(), "Missing perf metrics should be omitted from artifact row");
        Assertions.assertInstanceOf(JMHPrimaryScore.class, artifacts.get(0), "Primary score should be first");
        Assertions.assertInstanceOf(JMHAllocRateNorm.class, artifacts.get(1), "Alloc rate norm should be second");
    }
}
