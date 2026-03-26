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

class ComparisonTest {
    private static final String PRIMARY_SCORE = "1.23";
    private static final String TARGET = "Example::run";
    private static final String INSTRUCTIONS = "100";
    private static final String MEMORY_LOADS = "300";
    private static final String MEMORY_STORES = "500";

    @Test
    void rendersCsvWithHeaderAndRows() {
        final CsvComparison comparison = new CsvComparison(
                new StubAnalysis(
                        List.of(
                                ComparisonTest.TARGET,
                                ComparisonTest.PRIMARY_SCORE,
                                "42",
                                ComparisonTest.INSTRUCTIONS,
                                ComparisonTest.MEMORY_LOADS,
                                ComparisonTest.MEMORY_STORES,
                                "64"
                        ),
                        new StubResults(0.25d)
                ),
                new StubAnalysis(
                        List.of("Example, \"quoted\"", "3.21", "5", "200", "400", "600", "6"),
                        new StubResults(0.0d)
                )
        );
        final String header = "Target,\"JMH primary score, us/op\",\"Allocations, B/op\",\"Instructions, #/op\",\"Memory loads, #/op\",\"Memory stores, #/op\",\"Native code size, B\"";
        final String rowOne = ComparisonTest.TARGET + "," + ComparisonTest.PRIMARY_SCORE + ",42,"
                + ComparisonTest.INSTRUCTIONS + "," + ComparisonTest.MEMORY_LOADS + ","
                + ComparisonTest.MEMORY_STORES + ",64";
        final String rowTwo = "\"Example, \"\"quoted\"\"\",3.21,5,200,400,600,6";
        final String expected = String.join(System.lineSeparator(), header, rowOne, rowTwo);
        Assertions.assertEquals(expected, comparison.asCsv(), "Comparison CSV output should match expected content");
    }

    @Test
    void rendersCsvWithJitComparisonColumnsWhenEnabled() {
        final CsvComparison comparison = new CsvComparison(
                true,
                new StubAnalysis(
                        List.of(
                                ComparisonTest.TARGET,
                                ComparisonTest.PRIMARY_SCORE,
                                "42",
                                ComparisonTest.INSTRUCTIONS,
                                ComparisonTest.MEMORY_LOADS,
                                ComparisonTest.MEMORY_STORES,
                                "64"
                        ),
                        new StubResults(0.25d)
                ),
                new StubAnalysis(
                        List.of("ExampleRef::run", "3.21", "5", "200", "400", "600", "6"),
                        new StubResults(0.0d)
                )
        );
        final String header = "Target,\"JMH primary score, us/op\",\"Allocations, B/op\",\"Instructions, #/op\",\"Memory loads, #/op\",\"Memory stores, #/op\",\"Native code size, B\","
                + "JIT artifacts mean dissimilarity score,JIT artifacts max dissimilarity score";
        final String rowOne = ComparisonTest.TARGET + "," + ComparisonTest.PRIMARY_SCORE + ",42,"
                + ComparisonTest.INSTRUCTIONS + "," + ComparisonTest.MEMORY_LOADS + ","
                + ComparisonTest.MEMORY_STORES + ",64,Original,Original";
        final String rowTwo = "ExampleRef::run,3.21,5,200,400,600,6,0.25,0.25";
        final String expected = String.join(System.lineSeparator(), header, rowOne, rowTwo);
        Assertions.assertEquals(expected, comparison.asCsv(), "Opt-in comparison columns should be appended");
    }

    @Test
    void savesCsvToFile(@TempDir final Path tempDir) throws Exception {
        final CsvComparison comparison = new CsvComparison(
                new StubAnalysis(
                        List.of(
                                ComparisonTest.TARGET,
                                ComparisonTest.PRIMARY_SCORE,
                                "42",
                                ComparisonTest.INSTRUCTIONS,
                                ComparisonTest.MEMORY_LOADS,
                                ComparisonTest.MEMORY_STORES,
                                "64"
                        ),
                        new StubResults(0.0d)
                )
        );
        final Path output = tempDir.resolve("results.csv");
        comparison.saveAsCsv(output);
        final String content = Files.readString(output, StandardCharsets.UTF_8);
        Assertions.assertEquals(comparison.asCsv(), content, "Saved CSV should match generated content");
    }

    @Test
    void rendersCsvWithoutPerfColumnsWhenPerfResultsAreMissing() {
        final CsvComparison comparison = new CsvComparison(
                new StubAnalysis(
                        List.of("NoPerf::run", "1.23", "42", "64"),
                        StubResults.withoutPerf(0.25d)
                ),
                new StubAnalysis(
                        List.of("NoPerfRef::run", "3.21", "5", "6"),
                        StubResults.withoutPerf(0.0d)
                )
        );
        final String header = "Target,\"JMH primary score, us/op\",\"Allocations, B/op\",\"Native code size, B\"";
        final String rowOne = "NoPerf::run,1.23,42,64";
        final String rowTwo = "NoPerfRef::run,3.21,5,6";
        final String expected = String.join(System.lineSeparator(), header, rowOne, rowTwo);
        Assertions.assertEquals(
                expected,
                comparison.asCsv(),
                "Comparison CSV output should omit perf columns when perf results are missing"
        );
    }

    @Test
    void savesReadyRowsBeforeFailure(@TempDir final Path tempDir) throws Exception {
        final StubAnalysis original = new StubAnalysis(
                List.of(
                        ComparisonTest.TARGET,
                        ComparisonTest.PRIMARY_SCORE,
                        "42",
                        ComparisonTest.INSTRUCTIONS,
                        ComparisonTest.MEMORY_LOADS,
                        ComparisonTest.MEMORY_STORES,
                        "64"
                ),
                new StubResults(0.25d)
        );
        final StubResults failing = new StubResults(0.0d);
        final CsvComparison comparison = new CsvComparison(original, this.throwingAnalysis(failing));
        final Path output = tempDir.resolve("results.csv");
        Assertions.assertThrows(IllegalStateException.class, () -> comparison.saveAsCsv(output));
        final String header = "Target,\"JMH primary score, us/op\",\"Allocations, B/op\",\"Instructions, #/op\",\"Memory loads, #/op\",\"Memory stores, #/op\",\"Native code size, B\"";
        final String rowOne = ComparisonTest.TARGET + "," + ComparisonTest.PRIMARY_SCORE + ",42,"
                + ComparisonTest.INSTRUCTIONS + "," + ComparisonTest.MEMORY_LOADS + ","
                + ComparisonTest.MEMORY_STORES + ",64";
        final String expected = String.join(System.lineSeparator(), header, rowOne);
        final String content = Files.readString(output, StandardCharsets.UTF_8);
        Assertions.assertEquals(expected, content, "Rows ready before failure should stay persisted in file");
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
