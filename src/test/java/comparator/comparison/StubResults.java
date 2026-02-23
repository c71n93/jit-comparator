package comparator.comparison;

import comparator.JITResults;
import comparator.jitlog.LogResults;
import comparator.jmh.JMHAllocRateNorm;
import comparator.jmh.JMHInstructions;
import comparator.jmh.JMHMemoryLoads;
import comparator.jmh.JMHPrimaryScore;
import comparator.jmh.JMHResults;
import comparator.jmh.fixtures.JMHTarget;
import comparator.method.TargetMethod;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Stubbed JIT results for comparison CSV tests.
 */
final class StubResults {
    private static final String PRIMARY_SCORE_UNIT = "us/op";
    private static final String ALLOC_RATE_UNIT = "B";
    private static final String PERF_METRIC_UNIT = "#/op";
    private final double relDiff;
    private final boolean perfMetricsPresent;

    StubResults(final double relDiff) {
        this(relDiff, true);
    }

    private StubResults(final double relDiff, final boolean perfMetricsPresent) {
        this.relDiff = relDiff;
        this.perfMetricsPresent = perfMetricsPresent;
    }

    static StubResults withoutPerf(final double relDiff) {
        return new StubResults(relDiff, false);
    }

    JITResults asJitResults() {
        final JMHResults jmh;
        if (this.perfMetricsPresent) {
            jmh = new JMHResults(
                    new JMHPrimaryScore(0.0d, StubResults.PRIMARY_SCORE_UNIT),
                    new JMHAllocRateNorm(0.0d, StubResults.ALLOC_RATE_UNIT),
                    Optional.of(new JMHInstructions(0.0d, StubResults.PERF_METRIC_UNIT)),
                    Optional.of(new JMHMemoryLoads(0.0d, StubResults.PERF_METRIC_UNIT))
            );
        } else {
            jmh = new JMHResults(
                    new JMHPrimaryScore(0.0d, StubResults.PRIMARY_SCORE_UNIT),
                    new JMHAllocRateNorm(0.0d, StubResults.ALLOC_RATE_UNIT)
            );
        }
        return new JITResults(
                jmh,
                new LogResults(this.targetMethod(), Path.of("build", "test-jit.log"))
        ) {
            @Override
            public double relativeDifference(final JITResults other) {
                return StubResults.this.relDiff;
            }
        };
    }

    TargetMethod targetMethod() {
        final Path classpath = Path.of("build", "classes", "java", "test").toAbsolutePath();
        return new TargetMethod(classpath, JMHTarget.class.getName(), "succeed");
    }
}
