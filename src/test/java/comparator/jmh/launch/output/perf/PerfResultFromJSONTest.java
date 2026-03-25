package comparator.jmh.launch.output.perf;

import com.fasterxml.jackson.databind.ObjectMapper;
import comparator.Artifact;
import comparator.jmh.metrics.JMHPerfResults;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PerfResultFromJSONTest {
    private static final PerfMemoryEvents.MemoryEvents MEMORY_EVENTS = new PerfMemoryEvents.AvailableMemoryEvents(
            "loads", "stores"
    );
    private static final PerfMemoryEvents.MemoryEvents INTEL_MEMORY_EVENTS = new PerfMemoryEvents.AvailableMemoryEvents(
            "mem_inst_retired.all_loads",
            "mem_inst_retired.all_stores",
            List.of("mem_inst_retired.all_loads", "cpu_core/mem_inst_retired.all_loads"),
            List.of("mem_inst_retired.all_stores", "cpu_core/mem_inst_retired.all_stores")
    );
    private static final Path SOURCE = Path.of("result.json");

    @Test
    void parsesDirectPerfMetrics() throws Exception {
        final JMHPerfResults parsed = this.parsedResult(
                "{\"instructions:u\":{\"score\":3.0,\"scoreUnit\":\"#/op\"},"
                        + "\"loads:u\":{\"score\":4.0,\"scoreUnit\":\"#/op\"},"
                        + "\"stores:u\":{\"score\":5.0,\"scoreUnit\":\"#/op\"}}",
                true,
                true
        );
        final List<Artifact<?>> artifacts = parsed.asArtifactRow();
        Assertions.assertEquals(3, artifacts.size(), "All perf metrics should be present");
        Assertions.assertEquals(3.0d, artifacts.get(0).value().doubleValue(), 1.0e-12, "Instructions should match");
        Assertions.assertEquals(4.0d, artifacts.get(1).value().doubleValue(), 1.0e-12, "Loads should match");
        Assertions.assertEquals(5.0d, artifacts.get(2).value().doubleValue(), 1.0e-12, "Stores should match");
    }

    @Test
    void sumsHybridInstructionMetrics() throws Exception {
        final JMHPerfResults parsed = this.parsedResult(
                "{\"cpu_core/instructions:u\":{\"score\":3.0,\"scoreUnit\":\"#/op\"},"
                        + "\"cpu_atom/instructions:u\":{\"score\":4.0,\"scoreUnit\":\"#/op\"},"
                        + "\"loads:u\":{\"score\":4.0,\"scoreUnit\":\"#/op\"},"
                        + "\"stores:u\":{\"score\":5.0,\"scoreUnit\":\"#/op\"}}",
                true,
                true
        );
        Assertions.assertEquals(
                7.0d,
                parsed.asArtifactRow().get(0).value().doubleValue(),
                1.0e-12,
                "Hybrid instruction counters should be summed"
        );
    }

    @Test
    void sumsHybridInstructionMetricsWithTrailingPathSeparators() throws Exception {
        final JMHPerfResults parsed = this.parsedResult(
                "{\"cpu_core/instructions/:u\":{\"score\":3.0,\"scoreUnit\":\"#/op\"},"
                        + "\"cpu_atom/instructions/:u\":{\"score\":4.0,\"scoreUnit\":\"#/op\"},"
                        + "\"loads:u\":{\"score\":4.0,\"scoreUnit\":\"#/op\"},"
                        + "\"stores:u\":{\"score\":5.0,\"scoreUnit\":\"#/op\"}}",
                true,
                true
        );
        Assertions.assertEquals(
                7.0d,
                parsed.asArtifactRow().get(0).value().doubleValue(),
                1.0e-12,
                "Hybrid counters with trailing slash should be summed"
        );
    }

    @Test
    void parsesCpuCorePrefixedIntelMemoryMetrics() throws Exception {
        final JMHPerfResults parsed = this.parsedResult(
                "{\"instructions:u\":{\"score\":3.0,\"scoreUnit\":\"#/op\"},"
                        + "\"cpu_core/mem_inst_retired.all_loads/:u\":{\"score\":4.0,\"scoreUnit\":\"#/op\"},"
                        + "\"cpu_core/mem_inst_retired.all_stores/:u\":{\"score\":5.0,\"scoreUnit\":\"#/op\"}}",
                true,
                true,
                PerfResultFromJSONTest.INTEL_MEMORY_EVENTS
        );
        final List<Artifact<?>> artifacts = parsed.asArtifactRow();
        Assertions.assertEquals(3, artifacts.size(), "All perf metrics should be present");
        Assertions.assertEquals(3.0d, artifacts.get(0).value().doubleValue(), 1.0e-12, "Instructions should match");
        Assertions.assertEquals(4.0d, artifacts.get(1).value().doubleValue(), 1.0e-12, "Loads should match");
        Assertions.assertEquals(5.0d, artifacts.get(2).value().doubleValue(), 1.0e-12, "Stores should match");
    }

    @Test
    void omitsPerfMetricsWhenDisabled() throws Exception {
        final JMHPerfResults parsed = this.parsedResult(
                "{\"instructions:u\":{\"score\":3.0,\"scoreUnit\":\"#/op\"}}",
                false,
                true
        );
        Assertions.assertTrue(parsed.asArtifactRow().isEmpty(), "Disabled perf should omit all perf metrics");
    }

    @Test
    void omitsPerfMetricsWhenInstructionsMissing() throws Exception {
        final JMHPerfResults parsed = this.parsedResult(
                "{\"branches:u\":{\"score\":3.0,\"scoreUnit\":\"#/op\"}}",
                true,
                true
        );
        Assertions.assertTrue(parsed.asArtifactRow().isEmpty(), "Missing instructions should omit perf metrics");
    }

    @Test
    void keepsInstructionsWhenMemoryMetricsAreUnavailable() throws Exception {
        final JMHPerfResults parsed = this.parsedResult(
                "{\"instructions:u\":{\"score\":3.0,\"scoreUnit\":\"#/op\"}}",
                true,
                false
        );
        final List<Artifact<?>> artifacts = parsed.asArtifactRow();
        Assertions.assertEquals(1, artifacts.size(), "Instructions should remain when memory metrics are unavailable");
        Assertions.assertEquals(
                3.0d,
                artifacts.get(0).value().doubleValue(),
                1.0e-12,
                "Instructions should match"
        );
    }

    @Test
    void failsWhenRequiredMemoryMetricMissing() throws Exception {
        Assertions.assertThrows(
                IllegalStateException.class,
                () -> this.parsedResult(
                        "{\"instructions:u\":{\"score\":3.0,\"scoreUnit\":\"#/op\"},"
                                + "\"loads:u\":{\"score\":4.0,\"scoreUnit\":\"#/op\"}}",
                        true,
                        true
                ),
                "Missing required memory metrics should fail parsing"
        );
    }

    private JMHPerfResults parsedResult(final String json, final boolean perfEnabled,
            final boolean memoryMetricsAvailable) throws Exception {
        return this.parsedResult(
                json,
                perfEnabled,
                memoryMetricsAvailable,
                PerfResultFromJSONTest.MEMORY_EVENTS
        );
    }

    private JMHPerfResults parsedResult(final String json, final boolean perfEnabled,
            final boolean memoryMetricsAvailable, final PerfMemoryEvents.MemoryEvents memoryEvents) throws Exception {
        return new PerfResultFromJSON(
                new PerfSecondaryMetrics(new ObjectMapper().readTree(json)),
                PerfResultFromJSONTest.SOURCE,
                perfEnabled,
                memoryEvents,
                memoryMetricsAvailable
        ).parsedResult();
    }
}
