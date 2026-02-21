package comparator.comparison;

import comparator.Analysis;
import comparator.JITResults;
import comparator.jitlog.LogResults;
import comparator.jmh.JMHAllocRateNorm;
import comparator.jmh.JMHPrimaryScore;
import comparator.jmh.JMHResults;
import comparator.jmh.fixtures.JMHTarget;
import comparator.method.TargetMethod;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ComparisonsTest {
    @Test
    void concatenatesComparisonsAsCsv() {
        final Comparison first = new Comparison(
                new StubAnalysis(
                        List.of("One::run", "1.00", "2", "10", "100", "3"),
                        ComparisonsTest.stubResults(0.1d)
                ),
                new StubAnalysis(
                        List.of("OneRef::run", "1.10", "2", "11", "110", "3"),
                        ComparisonsTest.stubResults(0.0d)
                )
        );
        final Comparison second = new Comparison(
                new StubAnalysis(
                        List.of("Two::run", "2.00", "4", "20", "200", "6"),
                        ComparisonsTest.stubResults(0.2d)
                ),
                new StubAnalysis(
                        List.of("TwoRef::run", "2.10", "4", "21", "210", "6"),
                        ComparisonsTest.stubResults(0.0d)
                )
        );
        final Comparisons comparisons = new Comparisons(first, second);
        final String header = "Target,\"JMH primary score, us/op\",\"Allocations, B\",\"Instructions, #/op\",\"Memory loads, #/op\",\"Native code size, B\","
                + "JIT artifacts dissimilarity score";
        final String rowOne = "One::run,1.00,2,10,100,3,Original";
        final String rowTwo = "OneRef::run,1.10,2,11,110,3,0.1";
        final String rowThree = "Two::run,2.00,4,20,200,6,Original";
        final String rowFour = "TwoRef::run,2.10,4,21,210,6,0.2";
        final String expected = String.join(
                System.lineSeparator(),
                header,
                rowOne,
                rowTwo,
                header,
                rowThree,
                rowFour
        );
        Assertions.assertEquals(expected, comparisons.asCsv(), "Comparisons should concatenate rows");
    }

    private static TargetMethod targetMethod() {
        final Path classpath = Path.of("build", "classes", "java", "test").toAbsolutePath();
        return new TargetMethod(classpath, JMHTarget.class.getName(), "succeed");
    }

    private static JITResults stubResults(final double relDiff) {
        return new JITResults(
                new JMHResults(new JMHPrimaryScore(0.0d, "us/op"), new JMHAllocRateNorm(0.0d, "B")),
                new LogResults(ComparisonsTest.targetMethod(), Path.of("build", "test-jit.log"))
        ) {
            @Override
            public double relativeDifference(final JITResults other) {
                return relDiff;
            }
        };
    }

    private static final class StubAnalysis extends Analysis {
        private final List<String> row;
        private final JITResults results;

        StubAnalysis(final List<String> row, final JITResults results) {
            super(ComparisonsTest.targetMethod());
            this.row = List.copyOf(row);
            this.results = results;
        }

        @Override
        public List<String> asCsvRow() {
            return this.row;
        }

        @Override
        public JITResults results() {
            return this.results;
        }
    }
}
