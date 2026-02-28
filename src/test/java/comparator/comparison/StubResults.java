package comparator.comparison;

import comparator.Artifact;
import comparator.JITResults;
import comparator.jitlog.LogResults;
import comparator.jmh.JMHAllocRateNorm;
import comparator.jmh.JMHPrimaryScore;
import comparator.jmh.JMHResults;
import comparator.jmh.fixtures.JMHTarget;
import comparator.method.TargetMethod;
import java.nio.file.Path;
import java.util.List;

/**
 * Stubbed JIT results for comparison CSV tests.
 */
final class StubResults {
    private static final String PRIMARY_SCORE_UNIT = "us/op";
    private static final String ALLOC_RATE_UNIT = "B/op";
    private final double relDiff;

    StubResults(final double relDiff) {
        this.relDiff = relDiff;
    }

    static StubResults withoutPerf(final double relDiff) {
        return new StubResults(relDiff);
    }

    JITResults asJitResults() {
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
                return List.of(new ConstantRelDiffArtifact(StubResults.this.relDiff));
            }
        };
    }

    TargetMethod targetMethod() {
        final Path classpath = Path.of("build", "classes", "java", "test").toAbsolutePath();
        return new TargetMethod(classpath, JMHTarget.class.getName(), "succeed");
    }

    private static final class ConstantRelDiffArtifact implements Artifact<Double> {
        private final double relDiff;

        ConstantRelDiffArtifact(final double relDiff) {
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
        public double relativeDifference(final Artifact<?> other) {
            return this.relDiff;
        }
    }
}
