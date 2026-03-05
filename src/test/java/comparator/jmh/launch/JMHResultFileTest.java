package comparator.jmh.launch;

import comparator.Artifact;
import comparator.jmh.JMHResults;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JMHResultFileTest {
    private static final String JSON_WITH_PERF = "[{\"primaryMetric\":{\"score\":1.1,\"scoreUnit\":\"us/op\"},"
            + "\"secondaryMetrics\":{\"gc.alloc.rate.norm\":{\"score\":2.2,\"scoreUnit\":\"B/op\"},"
            + "\"instructions:u\":{\"score\":3.3,\"scoreUnit\":\"#/op\"},"
            + "\"mem_inst_retired.all_loads:u\":{\"score\":4.4,\"scoreUnit\":\"#/op\"},"
            + "\"mem_inst_retired.all_stores:u\":{\"score\":5.5,\"scoreUnit\":\"#/op\"}}}]";
    private static final String JSON_WITHOUT_PERF = "[{\"primaryMetric\":{\"score\":1.1,\"scoreUnit\":\"us/op\"},"
            + "\"secondaryMetrics\":{\"gc.alloc.rate.norm\":{\"score\":2.2,\"scoreUnit\":\"B/op\"}}}]";
    private static final String JSON_WITH_INCOMPLETE_PERF = "[{\"primaryMetric\":{\"score\":1.1,\"scoreUnit\":\"us/op\"},"
            + "\"secondaryMetrics\":{\"gc.alloc.rate.norm\":{\"score\":2.2,\"scoreUnit\":\"B/op\"},"
            + "\"instructions:u\":{\"score\":3.3,\"scoreUnit\":\"#/op\"},"
            + "\"mem_inst_retired.all_loads:u\":{\"score\":4.4,\"scoreUnit\":\"#/op\"},"
            + "\"mem_inst_retired.all_stores:p\":{\"score\":5.5,\"scoreUnit\":\"#/op\"}}}]";
    private static final String JSON_WITHOUT_PRIMARY = "[{\"secondaryMetrics\":{\"gc.alloc.rate.norm\":{\"score\":2.2,\"scoreUnit\":\"B/op\"}}}]";
    private static final String JSON_WITHOUT_ALLOC = "[{\"primaryMetric\":{\"score\":1.1,\"scoreUnit\":\"us/op\"}}]";

    @Test
    void parsesMetricsAsCsvRowFromJson(@TempDir final Path tempDir) throws Exception {
        Assumptions.assumeTrue(JMHResultFileTest.intelMemEventsAvailable(), "perf mem events are required");
        final JMHResults parsed = this.parsedResult(tempDir, "result.json", JMHResultFileTest.JSON_WITH_PERF, true);
        Assertions.assertEquals(
                List.of("1.1", "2.2", "3.3", "4.4", "5.5"), parsed.asCsvRow(), "JMH result should parse metrics"
        );
    }

    @Test
    void parsesMetricsAsArtifactRowFromJson(@TempDir final Path tempDir) throws Exception {
        Assumptions.assumeTrue(JMHResultFileTest.intelMemEventsAvailable(), "perf mem events are required");
        final JMHResults parsed = this.parsedResult(tempDir, "result.json", JMHResultFileTest.JSON_WITH_PERF, true);
        final List<Artifact<?>> artifacts = parsed.asArtifactRow();
        Assertions.assertEquals(5, artifacts.size(), "JMH result should expose five artifacts");
        Assertions.assertEquals(1.1d, artifacts.get(0).value().doubleValue(), 1.0e-12, "Primary metric should match");
        Assertions.assertEquals(
                2.2d, artifacts.get(1).value().doubleValue(), 1.0e-12, "Allocation metric should match"
        );
        Assertions.assertEquals(
                3.3d, artifacts.get(2).value().doubleValue(), 1.0e-12, "Instructions metric should match"
        );
        Assertions.assertEquals(
                4.4d, artifacts.get(3).value().doubleValue(), 1.0e-12, "Memory loads metric should match"
        );
        Assertions.assertEquals(
                5.5d, artifacts.get(4).value().doubleValue(), 1.0e-12, "Memory stores metric should match"
        );
    }

    @Test
    void parsesMetricsWithoutPerfMetricsAsCsvRow(@TempDir final Path tempDir) throws Exception {
        final JMHResults parsed = this.parsedResult(
                tempDir,
                "result-without-perf.json",
                JMHResultFileTest.JSON_WITHOUT_PERF
        );
        Assertions.assertEquals(List.of("1.1", "2.2"), parsed.asCsvRow(), "Missing perf metrics should be omitted");
    }

    @Test
    void parsesMetricsWithoutPerfMetricsAsArtifactRow(@TempDir final Path tempDir) throws Exception {
        final JMHResults parsed = this.parsedResult(
                tempDir,
                "result-without-perf.json",
                JMHResultFileTest.JSON_WITHOUT_PERF
        );
        final List<Artifact<?>> artifacts = parsed.asArtifactRow();
        Assertions.assertEquals(2, artifacts.size(), "Missing perf metrics should be omitted from artifact row");
        Assertions.assertEquals(1.1d, artifacts.get(0).value().doubleValue(), 1.0e-12, "Primary metric should match");
        Assertions.assertEquals(
                2.2d, artifacts.get(1).value().doubleValue(), 1.0e-12, "Allocation metric should match"
        );
    }

    @Test
    void ignoresIncompletePerfMetricsInCsvRow(@TempDir final Path tempDir) throws Exception {
        final JMHResults parsed = this.parsedResult(
                tempDir,
                "result-with-load-suffix.json",
                JMHResultFileTest.JSON_WITH_INCOMPLETE_PERF
        );
        Assertions.assertEquals(
                List.of("1.1", "2.2"), parsed.asCsvRow(),
                "Incomplete perf metric set should be ignored"
        );
    }

    @Test
    void ignoresIncompletePerfMetricsInArtifactRow(@TempDir final Path tempDir) throws Exception {
        final JMHResults parsed = this.parsedResult(
                tempDir,
                "result-with-load-suffix.json",
                JMHResultFileTest.JSON_WITH_INCOMPLETE_PERF
        );
        final List<Artifact<?>> artifacts = parsed.asArtifactRow();
        Assertions.assertEquals(2, artifacts.size(), "Incomplete perf metric set should be omitted from artifact row");
        Assertions.assertEquals(1.1d, artifacts.get(0).value().doubleValue(), 1.0e-12, "Primary metric should match");
        Assertions.assertEquals(
                2.2d, artifacts.get(1).value().doubleValue(), 1.0e-12, "Allocation metric should match"
        );
    }

    @Test
    void failsWhenPerfMetricsMissingAndPerfEnabled(@TempDir final Path tempDir) throws Exception {
        Assumptions.assumeTrue(JMHResultFileTest.intelMemEventsAvailable(), "perf mem events are required");
        final Path result = this.writeJson(tempDir, "missing-perf.json", JMHResultFileTest.JSON_WITHOUT_PERF);
        Assertions.assertThrows(
                IllegalStateException.class,
                () -> new JMHResultFile(result, true).parsedResult(),
                "Missing perf metrics should fail parsing when perf is enabled"
        );
    }

    @Test
    void failsWhenPerfMetricsIncompleteAndPerfEnabled(@TempDir final Path tempDir) throws Exception {
        Assumptions.assumeTrue(JMHResultFileTest.intelMemEventsAvailable(), "perf mem events are required");
        final Path result = this.writeJson(
                tempDir,
                "incomplete-perf.json",
                JMHResultFileTest.JSON_WITH_INCOMPLETE_PERF
        );
        Assertions.assertThrows(
                IllegalStateException.class,
                () -> new JMHResultFile(result, true).parsedResult(),
                "Incomplete perf metrics should fail parsing when perf is enabled"
        );
    }

    @Test
    void failsWhenPrimaryMetricMissing(@TempDir final Path tempDir) throws Exception {
        final Path result = this.writeJson(tempDir, "missing-primary.json", JMHResultFileTest.JSON_WITHOUT_PRIMARY);
        Assertions.assertThrows(
                IllegalStateException.class,
                () -> new JMHResultFile(result, false).parsedResult(),
                "Missing primary metric should fail parsing"
        );
    }

    @Test
    void failsWhenAllocRateMissing(@TempDir final Path tempDir) throws Exception {
        final Path result = this.writeJson(tempDir, "missing-alloc.json", JMHResultFileTest.JSON_WITHOUT_ALLOC);
        Assertions.assertThrows(
                IllegalStateException.class,
                () -> new JMHResultFile(result, false).parsedResult(),
                "Missing allocation metric should fail parsing"
        );
    }

    private JMHResults parsedResult(final Path tempDir, final String fileName, final String json) throws Exception {
        return this.parsedResult(tempDir, fileName, json, false);
    }

    private JMHResults parsedResult(final Path tempDir, final String fileName, final String json,
            final boolean perfEnabled) throws Exception {
        final Path result = this.writeJson(tempDir, fileName, json);
        return new JMHResultFile(result, perfEnabled).parsedResult();
    }

    private Path writeJson(final Path tempDir, final String fileName, final String json) throws Exception {
        final Path result = tempDir.resolve(fileName);
        Files.writeString(result, json, StandardCharsets.UTF_8);
        return result;
    }

    private static boolean intelMemEventsAvailable() {
        try {
            final Process process = new ProcessBuilder(
                    "perf", "stat", "-e",
                    "mem_inst_retired.all_loads,mem_inst_retired.all_stores", "echo", "1"
            ).start();
            process.getInputStream().readAllBytes();
            process.getErrorStream().readAllBytes();
            return process.waitFor() == 0;
        } catch (final InterruptedException exception) {
            Thread.currentThread().interrupt();
            return false;
        } catch (final IOException exception) {
            return false;
        }
    }
}
