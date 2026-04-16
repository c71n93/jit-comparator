package comparator.comparison;

import comparator.Artifact;
import comparator.JITResults;
import comparator.Metric;
import comparator.jitlog.LogResults;
import comparator.jmh.fixtures.JMHTarget;
import comparator.jmh.results.JMHAllocRateNorm;
import comparator.jmh.results.JMHPrimaryScore;
import comparator.jmh.results.JMHResults;
import comparator.method.TargetMethod;
import java.nio.file.Path;
import java.util.List;

/**
 * Stubbed JIT results for comparison CSV tests.
 */
final class StubResults {
    /** Primary score unit. */
    private static final String PRIMARY_SCORE_UNIT = "us/op";

    /** Alloc rate unit. */
    private static final String ALLOC_RATE_UNIT = "B/op";

    /** Relative difference. */
    private final double relDiff;

    /**
     * StubResults.
     *
     * @param relDiff relative difference
     */
    public StubResults(final double relDiff) {
        this.relDiff = relDiff;
    }

    /**
     * asJitResults.
     *
     * @return JIT results
     */
    public JITResults asJitResults() {
        final JMHResults jmh = new JMHResults(
            new JMHPrimaryScore(0.0d, StubResults.PRIMARY_SCORE_UNIT),
            new JMHAllocRateNorm(0.0d, StubResults.ALLOC_RATE_UNIT)
        );
        return new JITResults(
            jmh,
            new LogResults(this.targetMethod(), Path.of("build", "test-jit.log"))
        ) {
            @Override
            public List<Artifact<?>> asArtifactRow() {
                return List.of(new ConstantRelDiffMetric(StubResults.this.relDiff));
            }
        };
    }

    /**
     * targetMethod.
     *
     * @return target method
     */
    public TargetMethod targetMethod() {
        final Path classpath = Path.of("build", "classes", "java", "test").toAbsolutePath();
        return new TargetMethod(classpath, JMHTarget.class.getName(), "succeed");
    }

    private static final class ConstantRelDiffMetric implements Metric<Double> {
        /** Relative difference. */
        private final double relDiff;

        private ConstantRelDiffMetric(final double relDiff) {
            this.relDiff = relDiff;
        }

        @Override
        public Double value() {
            return 0.0d;
        }

        @Override
        public String headerCsv() {
            return "Stub relative difference";
        }

        @Override
        public double relativeDifference(final Metric<?> other) {
            return this.relDiff;
        }
    }
}
