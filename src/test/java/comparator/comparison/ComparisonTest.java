package comparator.comparison;

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
        final CsvComparison comparison = new CsvComparison(
                new StubAnalysis(
                        List.of("Example::run", "1.23", "42", "100", "300", "500", "64"),
                        new StubResults(0.25d)
                ),
                new StubAnalysis(
                        List.of("Example, \"quoted\"", "3.21", "5", "200", "400", "600", "6"),
                        new StubResults(0.0d)
                )
        );
        final String header = "Target,\"JMH primary score, us/op\",\"Allocations, B/op\",\"Instructions, #/op\",\"Memory loads, #/op\",\"Memory stores, #/op\",\"Native code size, B\","
                + "JIT artifacts mean dissimilarity score,JIT artifacts max dissimilarity score";
        final String rowOne = "Example::run,1.23,42,100,300,500,64,Original,Original";
        final String rowTwo = "\"Example, \"\"quoted\"\"\",3.21,5,200,400,600,6,0.25,0.25";
        final String expected = String.join(System.lineSeparator(), header, rowOne, rowTwo);
        Assertions.assertEquals(expected, comparison.asCsv(), "Comparison CSV output should match expected content");
    }

    @Test
    void savesCsvToFile(@TempDir final Path tempDir) throws Exception {
        final CsvComparison comparison = new CsvComparison(
                new StubAnalysis(
                        List.of("Example::run", "1.23", "42", "100", "300", "500", "64"),
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
        final String header = "Target,\"JMH primary score, us/op\",\"Allocations, B/op\",\"Native code size, B\","
                + "JIT artifacts mean dissimilarity score,JIT artifacts max dissimilarity score";
        final String rowOne = "NoPerf::run,1.23,42,64,Original,Original";
        final String rowTwo = "NoPerfRef::run,3.21,5,6,0.25,0.25";
        final String expected = String.join(System.lineSeparator(), header, rowOne, rowTwo);
        Assertions.assertEquals(
                expected,
                comparison.asCsv(),
                "Comparison CSV output should omit perf columns when perf results are missing"
        );
    }
}
