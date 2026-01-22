package comparator.comparison;

import comparator.Analysis;
import comparator.jmh.fixtures.JMHTarget;
import comparator.method.TargetMethod;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AnalysesToCsvTest {
    @Test
    void rendersCsvWithHeaderAndRows() {
        final AnalysesToCsv csv = new AnalysesToCsv(
                new StubAnalysis(List.of("Example::run", "1.23", "42", "64")),
                new StubAnalysis(List.of("Example, \"quoted\"", "3.21", "5", "6"))
        );
        final String header = "Target,\"JMH primary score, us/op\",\"Allocations, B\",\"Native code size, B\"";
        final String rowOne = "Example::run,1.23,42,64";
        final String rowTwo = "\"Example, \"\"quoted\"\"\",3.21,5,6";
        final String expected = String.join(System.lineSeparator(), header, rowOne, rowTwo);
        Assertions.assertEquals(expected, csv.value(), "CSV output should match expected content");
    }

    @Test
    void savesCsvToFile(@TempDir final Path tempDir) throws Exception {
        final AnalysesToCsv csv = new AnalysesToCsv(new StubAnalysis(List.of("Example::run", "1.23", "42", "64")));
        final Path output = tempDir.resolve("results.csv");
        csv.save(output);
        final String content = Files.readString(output, StandardCharsets.UTF_8);
        Assertions.assertEquals(csv.value(), content, "Saved CSV should match generated content");
    }

    private static TargetMethod targetMethod() {
        final Path classpath = Path.of("build", "classes", "java", "test").toAbsolutePath();
        return new TargetMethod(classpath, JMHTarget.class.getName(), "succeed");
    }

    private static final class StubAnalysis extends Analysis {
        private final List<String> row;

        StubAnalysis(final List<String> row) {
            super(AnalysesToCsvTest.targetMethod());
            this.row = List.copyOf(row);
        }

        @Override
        public List<String> asRow() {
            return this.row;
        }
    }
}
