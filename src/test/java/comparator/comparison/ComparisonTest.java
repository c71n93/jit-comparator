package comparator.comparison;

import comparator.Analysis;
import comparator.JITResults;
import comparator.jitlog.LogResults;
import comparator.jmh.JMHAllocRateNorm;
import comparator.jmh.JMHPrimaryScore;
import comparator.jmh.JMHResults;
import comparator.jmh.fixtures.JMHTarget;
import comparator.method.TargetMethod;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ComparisonTest {
    @Test
    void rendersCsvWithHeaderAndRows() {
        final Comparison comparison = new Comparison(
                new StubAnalysis(
                        List.of("Example::run", "1.23", "42", "100", "300", "64"),
                        ComparisonTest.stubResults(0.25d)
                ),
                new StubAnalysis(
                        List.of("Example, \"quoted\"", "3.21", "5", "200", "400", "6"),
                        ComparisonTest.stubResults(0.0d)
                )
        );
        final String header = "Target,\"JMH primary score, us/op\",\"Allocations, B\",\"Instructions, #/op\",\"Memory loads, #/op\",\"Native code size, B\","
                + "JIT artifacts dissimilarity score";
        final String rowOne = "Example::run,1.23,42,100,300,64,Original";
        final String rowTwo = "\"Example, \"\"quoted\"\"\",3.21,5,200,400,6,0.25";
        final String expected = String.join(System.lineSeparator(), header, rowOne, rowTwo);
        Assertions.assertEquals(expected, comparison.asCsv(), "Comparison CSV output should match expected content");
    }

    @Test
    void savesCsvToFile(@TempDir final Path tempDir) throws Exception {
        final Comparison comparison = new Comparison(
                new StubAnalysis(
                        List.of("Example::run", "1.23", "42", "100", "300", "64"),
                        ComparisonTest.stubResults(0.0d)
                )
        );
        final Path output = tempDir.resolve("results.csv");
        comparison.saveAsCsv(output);
        final String content = Files.readString(output, StandardCharsets.UTF_8);
        Assertions.assertEquals(comparison.asCsv(), content, "Saved CSV should match generated content");
    }

    private static TargetMethod targetMethod() {
        final Path classpath = Path.of("build", "classes", "java", "test").toAbsolutePath();
        return new TargetMethod(classpath, JMHTarget.class.getName(), "succeed");
    }

    private static JITResults stubResults(final double relDiff) {
        return new JITResults(
                new JMHResults(new JMHPrimaryScore(0.0d, "us/op"), new JMHAllocRateNorm(0.0d, "B")),
                new LogResults(ComparisonTest.targetMethod(), Path.of("build", "test-jit.log"))
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
            super(ComparisonTest.targetMethod());
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
