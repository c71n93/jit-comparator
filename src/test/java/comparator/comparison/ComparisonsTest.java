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
                        List.of("One::run", "1.00", "2", "10", "3"),
                        ComparisonsTest.stubResults(false)
                ),
                new StubAnalysis(
                        List.of("OneRef::run", "1.10", "2", "11", "3"),
                        ComparisonsTest.stubResults(false)
                )
        );
        final Comparison second = new Comparison(
                new StubAnalysis(
                        List.of("Two::run", "2.00", "4", "20", "6"),
                        ComparisonsTest.stubResults(true)
                ),
                new StubAnalysis(
                        List.of("TwoRef::run", "2.10", "4", "21", "6"),
                        ComparisonsTest.stubResults(true)
                )
        );
        final Comparisons comparisons = new Comparisons(first, second);
        final String header = "Target,\"JMH primary score, us/op\",\"Allocations, B\",\"Instructions, #/op\",\"Native code size, B\","
                + "JIT artifacts equivalent?";
        final String rowOne = "One::run,1.00,2,10,3,Original";
        final String rowTwo = "OneRef::run,1.10,2,11,3,false";
        final String rowThree = "Two::run,2.00,4,20,6,Original";
        final String rowFour = "TwoRef::run,2.10,4,21,6,true";
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

    private static JITResults stubResults(final boolean equivalent) {
        return new JITResults(
                new JMHResults(new JMHPrimaryScore(0.0d, "us/op"), new JMHAllocRateNorm(0.0d, "B")),
                new LogResults(ComparisonsTest.targetMethod(), Path.of("build", "test-jit.log"))
        ) {
            @Override
            public boolean isSame(final JITResults other) {
                return equivalent;
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
        public List<String> asRow() {
            return this.row;
        }

        @Override
        public JITResults results() {
            return this.results;
        }
    }
}
