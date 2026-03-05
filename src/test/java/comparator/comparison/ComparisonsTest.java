package comparator.comparison;

import comparator.Analysis;
import comparator.JITResults;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ComparisonsTest {
    @Test
    void concatenatesComparisonsAsCsv() {
        final CsvComparison first = new CsvComparison(
                new StubAnalysis(
                        List.of("One::run", "1.00", "2", "10", "100", "1000", "3"),
                        new StubResults(0.1d)
                ),
                new StubAnalysis(
                        List.of("OneRef::run", "1.10", "2", "11", "110", "1100", "3"),
                        new StubResults(0.0d)
                )
        );
        final CsvComparison second = new CsvComparison(
                new StubAnalysis(
                        List.of("Two::run", "2.00", "4", "20", "200", "2000", "6"),
                        new StubResults(0.2d)
                ),
                new StubAnalysis(
                        List.of("TwoRef::run", "2.10", "4", "21", "210", "2100", "6"),
                        new StubResults(0.0d)
                )
        );
        final CsvComparisons comparisons = new CsvComparisons(first, second);
        final String header = "Target,\"JMH primary score, us/op\",\"Allocations, B/op\",\"Instructions, #/op\",\"Memory loads, #/op\",\"Memory stores, #/op\",\"Native code size, B\","
                + "JIT artifacts mean dissimilarity score,JIT artifacts max dissimilarity score";
        final String rowOne = "One::run,1.00,2,10,100,1000,3,Original,Original";
        final String rowTwo = "OneRef::run,1.10,2,11,110,1100,3,0.1,0.1";
        final String rowThree = "Two::run,2.00,4,20,200,2000,6,Original,Original";
        final String rowFour = "TwoRef::run,2.10,4,21,210,2100,6,0.2,0.2";
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

    @Test
    void concatenatesComparisonsAsCsvWithoutPerfColumnsWhenPerfResultsAreMissing() {
        final CsvComparison first = new CsvComparison(
                new StubAnalysis(
                        List.of("One::run", "1.00", "2", "3"),
                        StubResults.withoutPerf(0.1d)
                ),
                new StubAnalysis(
                        List.of("OneRef::run", "1.10", "2", "3"),
                        StubResults.withoutPerf(0.0d)
                )
        );
        final CsvComparison second = new CsvComparison(
                new StubAnalysis(
                        List.of("Two::run", "2.00", "4", "6"),
                        StubResults.withoutPerf(0.2d)
                ),
                new StubAnalysis(
                        List.of("TwoRef::run", "2.10", "4", "6"),
                        StubResults.withoutPerf(0.0d)
                )
        );
        final CsvComparisons comparisons = new CsvComparisons(first, second);
        final String header = "Target,\"JMH primary score, us/op\",\"Allocations, B/op\",\"Native code size, B\","
                + "JIT artifacts mean dissimilarity score,JIT artifacts max dissimilarity score";
        final String rowOne = "One::run,1.00,2,3,Original,Original";
        final String rowTwo = "OneRef::run,1.10,2,3,0.1,0.1";
        final String rowThree = "Two::run,2.00,4,6,Original,Original";
        final String rowFour = "TwoRef::run,2.10,4,6,0.2,0.2";
        final String expected = String.join(
                System.lineSeparator(),
                header,
                rowOne,
                rowTwo,
                header,
                rowThree,
                rowFour
        );
        Assertions.assertEquals(
                expected,
                comparisons.asCsv(),
                "Comparisons should omit perf columns when perf results are missing"
        );
    }

    @Test
    void savesAllReadyRowsBeforeFailure(@TempDir final Path tempDir) throws Exception {
        final CsvComparison first = new CsvComparison(
                new StubAnalysis(
                        List.of("One::run", "1.00", "2", "10", "100", "1000", "3"),
                        new StubResults(0.1d)
                ),
                new StubAnalysis(
                        List.of("OneRef::run", "1.10", "2", "11", "110", "1100", "3"),
                        new StubResults(0.0d)
                )
        );
        final StubAnalysis secondOriginal = new StubAnalysis(
                List.of("Two::run", "2.00", "4", "20", "200", "2000", "6"),
                new StubResults(0.2d)
        );
        final StubResults failing = new StubResults(0.0d);
        final CsvComparison second = new CsvComparison(secondOriginal, this.throwingAnalysis(failing));
        final CsvComparisons comparisons = new CsvComparisons(first, second);
        final Path output = tempDir.resolve("results.csv");
        Assertions.assertThrows(IllegalStateException.class, () -> comparisons.saveAsCsv(output));
        final String header = "Target,\"JMH primary score, us/op\",\"Allocations, B/op\",\"Instructions, #/op\",\"Memory loads, #/op\",\"Memory stores, #/op\",\"Native code size, B\","
                + "JIT artifacts mean dissimilarity score,JIT artifacts max dissimilarity score";
        final String rowOne = "One::run,1.00,2,10,100,1000,3,Original,Original";
        final String rowTwo = "OneRef::run,1.10,2,11,110,1100,3,0.1,0.1";
        final String rowThree = "Two::run,2.00,4,20,200,2000,6,Original,Original";
        final String expected = String.join(System.lineSeparator(), header, rowOne, rowTwo, header, rowThree);
        final String content = Files.readString(output, StandardCharsets.UTF_8);
        Assertions.assertEquals(
                expected,
                content,
                "Comparisons CSV should preserve each row written before failure"
        );
    }

    private Analysis throwingAnalysis(final StubResults results) {
        return new Analysis(results.targetMethod()) {
            private final JITResults jitResults = results.asJitResults();

            @Override
            public JITResults results() {
                return this.jitResults;
            }

            @Override
            public List<String> asCsvRow() {
                throw new IllegalStateException("Failure during CSV row generation");
            }
        };
    }
}
