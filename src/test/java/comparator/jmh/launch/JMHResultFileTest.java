package comparator.jmh.launch;

import comparator.Artifact;
import comparator.jmh.launch.output.JMHResultFile;
import comparator.jmh.launch.output.perf.PerfMemoryEvents;
import comparator.jmh.results.JMHResults;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JMHResultFileTest {
    private static final String PERF_EVENTS_REQUIRED = "perf mem events are required";
    private static final String PRIMARY_SCORE_CSV = "1.1";
    private static final String PRIMARY_SCORE_ERROR_JSON = "1.1";
    private static final String PRIMARY_RELATIVE_ERROR_CSV = "1.0";
    private static final String ALLOC_SCORE_CSV = "2.2";
    private static final String ALLOC_SCORE_ERROR_JSON = "2.2";
    private static final String ALLOC_RELATIVE_ERROR_CSV = "1.0";
    private static final String INSTRUCTIONS_SCORE_CSV = "3.3";
    private static final String HYBRID_CORE_INSTRUCTIONS_SCORE_CSV = "1.0";
    private static final String HYBRID_ATOM_INSTRUCTIONS_SCORE_CSV = "2.0";
    private static final String HYBRID_INSTRUCTIONS_SCORE_CSV = "3.0";
    private static final String LOADS_SCORE_CSV = "4.4";
    private static final String STORES_SCORE_CSV = "5.5";
    private static final String USER_SUFFIX = ":u";
    private static final String KERNEL_SUFFIX = ":k";
    private static final String JSON_PRIMARY_PREFIX = "[{\"primaryMetric\":{\"score\":";
    private static final String JSON_SCORE_ERROR_FIELD = ",\"scoreError\":";
    private static final String JSON_PRIMARY_SUFFIX = ",\"scoreUnit\":\"us/op\"},";
    private static final String JSON_PRIMARY_END = ",\"scoreUnit\":\"us/op\"}";
    private static final String JSON_SECONDARY_PREFIX = "\"secondaryMetrics\":{";
    private static final String JSON_ALLOC_PREFIX = "\"gc.alloc.rate.norm\":{\"score\":";
    private static final String JSON_ALLOC_SUFFIX = ",\"scoreUnit\":\"B/op\"}";
    private static final String JSON_OBJECT_SUFFIX = "}}]";
    private static final String JSON_METRIC_SCORE_PREFIX = "\":{\"score\":";
    private static final String JSON_OP_UNIT_SUFFIX = ",\"scoreUnit\":\"#/op\"}";
    private static final String JSON_OP_UNIT_SUFFIX_WITH_COMMA = JMHResultFileTest.JSON_OP_UNIT_SUFFIX + ",";
    private static final String INSTRUCTIONS_EVENT = "instructions";
    private static final String HYBRID_CORE_INSTRUCTIONS_EVENT = "cpu_core/instructions";
    private static final String HYBRID_ATOM_INSTRUCTIONS_EVENT = "cpu_atom/instructions";
    private static final String MEM_LOADS_EVENT = PerfMemoryEvents.events().loadEventName();
    private static final String MEM_STORES_EVENT = PerfMemoryEvents.events().storeEventName();
    private static final String MEM_LOADS_METRIC = JMHResultFileTest.MEM_LOADS_EVENT + JMHResultFileTest.USER_SUFFIX;
    private static final String MEM_STORES_INVALID_METRIC = JMHResultFileTest.MEM_STORES_EVENT + ".invalid"
            + JMHResultFileTest.USER_SUFFIX;
    private static final List<String> CSV_WITH_PERF = List.of(
            JMHResultFileTest.PRIMARY_SCORE_CSV,
            JMHResultFileTest.PRIMARY_RELATIVE_ERROR_CSV,
            JMHResultFileTest.ALLOC_SCORE_CSV,
            JMHResultFileTest.ALLOC_RELATIVE_ERROR_CSV,
            JMHResultFileTest.INSTRUCTIONS_SCORE_CSV,
            JMHResultFileTest.LOADS_SCORE_CSV,
            JMHResultFileTest.STORES_SCORE_CSV
    );
    private static final List<String> CSV_WITHOUT_PERF = List.of(
            JMHResultFileTest.PRIMARY_SCORE_CSV,
            JMHResultFileTest.PRIMARY_RELATIVE_ERROR_CSV,
            JMHResultFileTest.ALLOC_SCORE_CSV,
            JMHResultFileTest.ALLOC_RELATIVE_ERROR_CSV
    );
    private static final List<String> CSV_WITH_HYBRID_INSTRUCTIONS = List.of(
            JMHResultFileTest.PRIMARY_SCORE_CSV,
            JMHResultFileTest.PRIMARY_RELATIVE_ERROR_CSV,
            JMHResultFileTest.ALLOC_SCORE_CSV,
            JMHResultFileTest.ALLOC_RELATIVE_ERROR_CSV,
            JMHResultFileTest.HYBRID_INSTRUCTIONS_SCORE_CSV,
            JMHResultFileTest.LOADS_SCORE_CSV,
            JMHResultFileTest.STORES_SCORE_CSV
    );
    private static final String JSON_WITH_PERF = JMHResultFileTest.jsonWithPerfMetrics(
            JMHResultFileTest.USER_SUFFIX, JMHResultFileTest.USER_SUFFIX, JMHResultFileTest.USER_SUFFIX
    );
    private static final String JSON_WITH_PERF_KERNEL_SUFFIX = JMHResultFileTest.jsonWithPerfMetrics(
            JMHResultFileTest.KERNEL_SUFFIX, JMHResultFileTest.KERNEL_SUFFIX, JMHResultFileTest.KERNEL_SUFFIX
    );
    private static final String JSON_WITH_PERF_NO_SUFFIX = JMHResultFileTest.jsonWithPerfMetrics("", "", "");
    private static final String JSON_WITH_HYBRID_INSTRUCTIONS = JMHResultFileTest.JSON_PRIMARY_PREFIX
            + JMHResultFileTest.PRIMARY_SCORE_CSV + JMHResultFileTest.JSON_SCORE_ERROR_FIELD
            + JMHResultFileTest.PRIMARY_SCORE_ERROR_JSON + JMHResultFileTest.JSON_PRIMARY_SUFFIX
            + JMHResultFileTest.JSON_SECONDARY_PREFIX + JMHResultFileTest.JSON_ALLOC_PREFIX
            + JMHResultFileTest.ALLOC_SCORE_CSV + JMHResultFileTest.JSON_SCORE_ERROR_FIELD
            + JMHResultFileTest.ALLOC_SCORE_ERROR_JSON + JMHResultFileTest.JSON_ALLOC_SUFFIX + ","
            + "\"" + JMHResultFileTest.HYBRID_CORE_INSTRUCTIONS_EVENT + JMHResultFileTest.USER_SUFFIX
            + JMHResultFileTest.JSON_METRIC_SCORE_PREFIX + JMHResultFileTest.HYBRID_CORE_INSTRUCTIONS_SCORE_CSV
            + JMHResultFileTest.JSON_OP_UNIT_SUFFIX_WITH_COMMA + "\""
            + JMHResultFileTest.HYBRID_ATOM_INSTRUCTIONS_EVENT + JMHResultFileTest.USER_SUFFIX
            + JMHResultFileTest.JSON_METRIC_SCORE_PREFIX + JMHResultFileTest.HYBRID_ATOM_INSTRUCTIONS_SCORE_CSV
            + JMHResultFileTest.JSON_OP_UNIT_SUFFIX_WITH_COMMA + "\"" + JMHResultFileTest.MEM_LOADS_EVENT
            + JMHResultFileTest.USER_SUFFIX + JMHResultFileTest.JSON_METRIC_SCORE_PREFIX
            + JMHResultFileTest.LOADS_SCORE_CSV + JMHResultFileTest.JSON_OP_UNIT_SUFFIX_WITH_COMMA
            + "\"" + JMHResultFileTest.MEM_STORES_EVENT + JMHResultFileTest.USER_SUFFIX
            + JMHResultFileTest.JSON_METRIC_SCORE_PREFIX + JMHResultFileTest.STORES_SCORE_CSV
            + JMHResultFileTest.JSON_OP_UNIT_SUFFIX + JMHResultFileTest.JSON_OBJECT_SUFFIX;
    private static final String JSON_WITHOUT_PERF = JMHResultFileTest.JSON_PRIMARY_PREFIX
            + JMHResultFileTest.PRIMARY_SCORE_CSV + JMHResultFileTest.JSON_SCORE_ERROR_FIELD
            + JMHResultFileTest.PRIMARY_SCORE_ERROR_JSON + JMHResultFileTest.JSON_PRIMARY_SUFFIX
            + JMHResultFileTest.JSON_SECONDARY_PREFIX + JMHResultFileTest.JSON_ALLOC_PREFIX
            + JMHResultFileTest.ALLOC_SCORE_CSV + JMHResultFileTest.JSON_SCORE_ERROR_FIELD
            + JMHResultFileTest.ALLOC_SCORE_ERROR_JSON + JMHResultFileTest.JSON_ALLOC_SUFFIX
            + JMHResultFileTest.JSON_OBJECT_SUFFIX;
    private static final String JSON_WITH_INCOMPLETE_PERF = JMHResultFileTest.JSON_PRIMARY_PREFIX
            + JMHResultFileTest.PRIMARY_SCORE_CSV + JMHResultFileTest.JSON_SCORE_ERROR_FIELD
            + JMHResultFileTest.PRIMARY_SCORE_ERROR_JSON + JMHResultFileTest.JSON_PRIMARY_SUFFIX
            + JMHResultFileTest.JSON_SECONDARY_PREFIX + JMHResultFileTest.JSON_ALLOC_PREFIX
            + JMHResultFileTest.ALLOC_SCORE_CSV + JMHResultFileTest.JSON_SCORE_ERROR_FIELD
            + JMHResultFileTest.ALLOC_SCORE_ERROR_JSON + JMHResultFileTest.JSON_ALLOC_SUFFIX + ","
            + "\"" + JMHResultFileTest.INSTRUCTIONS_EVENT + JMHResultFileTest.USER_SUFFIX
            + JMHResultFileTest.JSON_METRIC_SCORE_PREFIX + JMHResultFileTest.INSTRUCTIONS_SCORE_CSV
            + JMHResultFileTest.JSON_OP_UNIT_SUFFIX_WITH_COMMA + "\"" + JMHResultFileTest.MEM_LOADS_METRIC
            + JMHResultFileTest.JSON_METRIC_SCORE_PREFIX + JMHResultFileTest.LOADS_SCORE_CSV
            + JMHResultFileTest.JSON_OP_UNIT_SUFFIX_WITH_COMMA + "\"" + JMHResultFileTest.MEM_STORES_INVALID_METRIC
            + JMHResultFileTest.JSON_METRIC_SCORE_PREFIX + JMHResultFileTest.STORES_SCORE_CSV
            + JMHResultFileTest.JSON_OP_UNIT_SUFFIX + JMHResultFileTest.JSON_OBJECT_SUFFIX;
    private static final String JSON_WITHOUT_PRIMARY = "[{" + JMHResultFileTest.JSON_SECONDARY_PREFIX
            + JMHResultFileTest.JSON_ALLOC_PREFIX + JMHResultFileTest.ALLOC_SCORE_CSV
            + JMHResultFileTest.JSON_SCORE_ERROR_FIELD + JMHResultFileTest.ALLOC_SCORE_ERROR_JSON
            + JMHResultFileTest.JSON_ALLOC_SUFFIX + JMHResultFileTest.JSON_OBJECT_SUFFIX;
    private static final String JSON_WITHOUT_PRIMARY_ERROR = JMHResultFileTest.JSON_PRIMARY_PREFIX
            + JMHResultFileTest.PRIMARY_SCORE_CSV + JMHResultFileTest.JSON_PRIMARY_SUFFIX
            + JMHResultFileTest.JSON_SECONDARY_PREFIX + JMHResultFileTest.JSON_ALLOC_PREFIX
            + JMHResultFileTest.ALLOC_SCORE_CSV + JMHResultFileTest.JSON_SCORE_ERROR_FIELD
            + JMHResultFileTest.ALLOC_SCORE_ERROR_JSON + JMHResultFileTest.JSON_ALLOC_SUFFIX
            + JMHResultFileTest.JSON_OBJECT_SUFFIX;
    private static final String JSON_WITHOUT_ALLOC = JMHResultFileTest.JSON_PRIMARY_PREFIX
            + JMHResultFileTest.PRIMARY_SCORE_CSV + JMHResultFileTest.JSON_SCORE_ERROR_FIELD
            + JMHResultFileTest.PRIMARY_SCORE_ERROR_JSON + JMHResultFileTest.JSON_PRIMARY_END + "}]";
    private static final String JSON_WITHOUT_ALLOC_ERROR = JMHResultFileTest.JSON_PRIMARY_PREFIX
            + JMHResultFileTest.PRIMARY_SCORE_CSV + JMHResultFileTest.JSON_SCORE_ERROR_FIELD
            + JMHResultFileTest.PRIMARY_SCORE_ERROR_JSON + JMHResultFileTest.JSON_PRIMARY_SUFFIX
            + JMHResultFileTest.JSON_SECONDARY_PREFIX + JMHResultFileTest.JSON_ALLOC_PREFIX
            + JMHResultFileTest.ALLOC_SCORE_CSV + JMHResultFileTest.JSON_ALLOC_SUFFIX
            + JMHResultFileTest.JSON_OBJECT_SUFFIX;

    @Test
    void parsesMetricsAsCsvRowFromJson(@TempDir final Path tempDir) throws Exception {
        Assumptions.assumeTrue(PerfMemoryEvents.memEventsAvailable(), JMHResultFileTest.PERF_EVENTS_REQUIRED);
        final JMHResults parsed = this.parsedResult(tempDir, "result.json", JMHResultFileTest.JSON_WITH_PERF, true);
        Assertions.assertEquals(
                JMHResultFileTest.CSV_WITH_PERF, parsed.asCsvRow(), "JMH result should parse metrics"
        );
    }

    @Test
    void parsesMetricsAsArtifactRowFromJson(@TempDir final Path tempDir) throws Exception {
        Assumptions.assumeTrue(PerfMemoryEvents.memEventsAvailable(), JMHResultFileTest.PERF_EVENTS_REQUIRED);
        final JMHResults parsed = this.parsedResult(tempDir, "result.json", JMHResultFileTest.JSON_WITH_PERF, true);
        final List<Artifact<?>> artifacts = parsed.asArtifactRow();
        Assertions.assertEquals(7, artifacts.size(), "JMH result should expose metrics and relative errors");
        Assertions.assertEquals(1.1d, artifacts.get(0).value().doubleValue(), 1.0e-12, "Primary metric should match");
        Assertions.assertEquals(
                1.0d, artifacts.get(1).value().doubleValue(), 1.0e-12, "Primary relative error should match"
        );
        Assertions.assertEquals(
                2.2d, artifacts.get(2).value().doubleValue(), 1.0e-12, "Allocation metric should match"
        );
        Assertions.assertEquals(
                1.0d, artifacts.get(3).value().doubleValue(), 1.0e-12, "Allocation relative error should match"
        );
        Assertions.assertEquals(
                3.3d, artifacts.get(4).value().doubleValue(), 1.0e-12, "Instructions metric should match"
        );
        Assertions.assertEquals(
                4.4d, artifacts.get(5).value().doubleValue(), 1.0e-12, "Memory loads metric should match"
        );
        Assertions.assertEquals(
                5.5d, artifacts.get(6).value().doubleValue(), 1.0e-12, "Memory stores metric should match"
        );
    }

    @Test
    void parsesPerfMetricsWithKernelSuffixes(@TempDir final Path tempDir) throws Exception {
        Assumptions.assumeTrue(PerfMemoryEvents.memEventsAvailable(), JMHResultFileTest.PERF_EVENTS_REQUIRED);
        final JMHResults parsed = this.parsedResult(
                tempDir,
                "result-kernel-suffix.json",
                JMHResultFileTest.JSON_WITH_PERF_KERNEL_SUFFIX,
                true
        );
        Assertions.assertEquals(
                JMHResultFileTest.CSV_WITH_PERF, parsed.asCsvRow(),
                "Perf metrics with :k suffix should parse correctly"
        );
    }

    @Test
    void parsesPerfMetricsWithoutSuffixes(@TempDir final Path tempDir) throws Exception {
        Assumptions.assumeTrue(PerfMemoryEvents.memEventsAvailable(), JMHResultFileTest.PERF_EVENTS_REQUIRED);
        final JMHResults parsed = this.parsedResult(
                tempDir,
                "result-no-suffix.json",
                JMHResultFileTest.JSON_WITH_PERF_NO_SUFFIX,
                true
        );
        Assertions.assertEquals(
                JMHResultFileTest.CSV_WITH_PERF, parsed.asCsvRow(),
                "Perf metrics without suffix should parse correctly"
        );
    }

    @Test
    void parsesHybridInstructionMetrics(@TempDir final Path tempDir) throws Exception {
        Assumptions.assumeTrue(PerfMemoryEvents.memEventsAvailable(), JMHResultFileTest.PERF_EVENTS_REQUIRED);
        final JMHResults parsed = this.parsedResult(
                tempDir,
                "result-hybrid-instructions.json",
                JMHResultFileTest.JSON_WITH_HYBRID_INSTRUCTIONS,
                true
        );
        Assertions.assertEquals(
                JMHResultFileTest.CSV_WITH_HYBRID_INSTRUCTIONS,
                parsed.asCsvRow(),
                "Hybrid instruction counters should be aggregated into the instructions metric"
        );
    }

    @Test
    void parsesMetricsWithoutPerfMetricsAsCsvRow(@TempDir final Path tempDir) throws Exception {
        final JMHResults parsed = this.parsedResult(
                tempDir,
                "result-without-perf.json",
                JMHResultFileTest.JSON_WITHOUT_PERF
        );
        Assertions.assertEquals(
                JMHResultFileTest.CSV_WITHOUT_PERF, parsed.asCsvRow(), "Missing perf metrics should be omitted"
        );
    }

    @Test
    void parsesMetricsWithoutPerfMetricsAsArtifactRow(@TempDir final Path tempDir) throws Exception {
        final JMHResults parsed = this.parsedResult(
                tempDir,
                "result-without-perf.json",
                JMHResultFileTest.JSON_WITHOUT_PERF
        );
        final List<Artifact<?>> artifacts = parsed.asArtifactRow();
        Assertions.assertEquals(4, artifacts.size(), "Missing perf metrics should be omitted from artifact row");
        Assertions.assertEquals(1.1d, artifacts.get(0).value().doubleValue(), 1.0e-12, "Primary metric should match");
        Assertions.assertEquals(
                1.0d, artifacts.get(1).value().doubleValue(), 1.0e-12, "Primary relative error should match"
        );
        Assertions.assertEquals(
                2.2d, artifacts.get(2).value().doubleValue(), 1.0e-12, "Allocation metric should match"
        );
        Assertions.assertEquals(
                1.0d, artifacts.get(3).value().doubleValue(), 1.0e-12, "Allocation relative error should match"
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
                JMHResultFileTest.CSV_WITHOUT_PERF, parsed.asCsvRow(),
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
        Assertions.assertEquals(4, artifacts.size(), "Incomplete perf metric set should be omitted from artifact row");
        Assertions.assertEquals(1.1d, artifacts.get(0).value().doubleValue(), 1.0e-12, "Primary metric should match");
        Assertions.assertEquals(
                1.0d, artifacts.get(1).value().doubleValue(), 1.0e-12, "Primary relative error should match"
        );
        Assertions.assertEquals(
                2.2d, artifacts.get(2).value().doubleValue(), 1.0e-12, "Allocation metric should match"
        );
        Assertions.assertEquals(
                1.0d, artifacts.get(3).value().doubleValue(), 1.0e-12, "Allocation relative error should match"
        );
    }

    @Test
    void omitsPerfMetricsWhenInstructionsMissingAndPerfEnabled(@TempDir final Path tempDir) throws Exception {
        final JMHResults parsed = this.parsedResult(
                tempDir,
                "missing-perf.json",
                JMHResultFileTest.JSON_WITHOUT_PERF,
                true
        );
        Assertions.assertEquals(
                JMHResultFileTest.CSV_WITHOUT_PERF,
                parsed.asCsvRow(),
                "Missing instruction counters should skip perf metrics when perf is enabled"
        );
    }

    @Test
    void failsWhenPerfMetricsIncompleteAndPerfEnabled(@TempDir final Path tempDir) throws Exception {
        Assumptions.assumeTrue(PerfMemoryEvents.memEventsAvailable(), JMHResultFileTest.PERF_EVENTS_REQUIRED);
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
    void failsWhenPrimaryMetricErrorMissing(@TempDir final Path tempDir) throws Exception {
        final Path result = this.writeJson(
                tempDir,
                "missing-primary-error.json",
                JMHResultFileTest.JSON_WITHOUT_PRIMARY_ERROR
        );
        Assertions.assertThrows(
                IllegalStateException.class,
                () -> new JMHResultFile(result, false).parsedResult(),
                "Missing primary metric error should fail parsing"
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

    @Test
    void failsWhenAllocRateErrorMissing(@TempDir final Path tempDir) throws Exception {
        final Path result = this.writeJson(
                tempDir,
                "missing-alloc-error.json",
                JMHResultFileTest.JSON_WITHOUT_ALLOC_ERROR
        );
        Assertions.assertThrows(
                IllegalStateException.class,
                () -> new JMHResultFile(result, false).parsedResult(),
                "Missing allocation metric error should fail parsing"
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

    private static String jsonWithPerfMetrics(final String instructionsSuffix, final String loadSuffix,
            final String storeSuffix) {
        return JMHResultFileTest.JSON_PRIMARY_PREFIX + JMHResultFileTest.PRIMARY_SCORE_CSV
                + JMHResultFileTest.JSON_SCORE_ERROR_FIELD + JMHResultFileTest.PRIMARY_SCORE_ERROR_JSON
                + JMHResultFileTest.JSON_PRIMARY_SUFFIX + JMHResultFileTest.JSON_SECONDARY_PREFIX
                + JMHResultFileTest.JSON_ALLOC_PREFIX + JMHResultFileTest.ALLOC_SCORE_CSV
                + JMHResultFileTest.JSON_SCORE_ERROR_FIELD + JMHResultFileTest.ALLOC_SCORE_ERROR_JSON
                + JMHResultFileTest.JSON_ALLOC_SUFFIX + ","
                + "\"" + JMHResultFileTest.INSTRUCTIONS_EVENT + instructionsSuffix
                + JMHResultFileTest.JSON_METRIC_SCORE_PREFIX + JMHResultFileTest.INSTRUCTIONS_SCORE_CSV
                + JMHResultFileTest.JSON_OP_UNIT_SUFFIX_WITH_COMMA + "\"" + JMHResultFileTest.MEM_LOADS_EVENT
                + loadSuffix + JMHResultFileTest.JSON_METRIC_SCORE_PREFIX + JMHResultFileTest.LOADS_SCORE_CSV
                + JMHResultFileTest.JSON_OP_UNIT_SUFFIX_WITH_COMMA
                + "\"" + JMHResultFileTest.MEM_STORES_EVENT + storeSuffix
                + JMHResultFileTest.JSON_METRIC_SCORE_PREFIX + JMHResultFileTest.STORES_SCORE_CSV
                + JMHResultFileTest.JSON_OP_UNIT_SUFFIX + JMHResultFileTest.JSON_OBJECT_SUFFIX;
    }
}
