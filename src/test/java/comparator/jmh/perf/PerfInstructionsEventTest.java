package comparator.jmh.perf;

import com.fasterxml.jackson.databind.ObjectMapper;

import comparator.jmh.launch.output.perf.PerfInstructionsEvent;
import comparator.jmh.launch.output.perf.PerfSecondaryMetrics;
import comparator.jmh.metrics.JMHInstructions;

import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PerfInstructionsEventTest {
    private static final String PERF_UNIT = "#/op";

    @Test
    void parsesDirectInstructionsMetric() throws Exception {
        final Optional<JMHInstructions> instructions = PerfInstructionsEvent.metric(
                this.secondaryMetrics("{\"instructions:u\":{\"score\":3.0,\"scoreUnit\":\"#/op\"}}")
        );
        Assertions.assertTrue(instructions.isPresent(), "Direct instructions metric should be parsed");
        final JMHInstructions metric = instructions.orElseThrow();
        Assertions.assertEquals(3.0d, metric.value(), 1.0e-12, "Instructions value should match");
        Assertions.assertEquals(
                "Instructions, " + PerfInstructionsEventTest.PERF_UNIT,
                metric.headerCsv(),
                "Instructions unit should match"
        );
    }

    @Test
    void sumsHybridInstructionsMetrics() throws Exception {
        final Optional<JMHInstructions> instructions = PerfInstructionsEvent.metric(
                this.secondaryMetrics(
                        "{\"cpu_core/instructions:u\":{\"score\":3.0,\"scoreUnit\":\"#/op\"},"
                                + "\"cpu_atom/instructions:u\":{\"score\":4.0,\"scoreUnit\":\"#/op\"}}"
                )
        );
        Assertions.assertTrue(instructions.isPresent(), "Hybrid instructions metrics should be parsed");
        Assertions.assertEquals(7.0d, instructions.orElseThrow().value(), 1.0e-12, "Hybrid counters should be summed");
    }

    @Test
    void sumsHybridInstructionsMetricsWithTrailingPathSeparators() throws Exception {
        final Optional<JMHInstructions> instructions = PerfInstructionsEvent.metric(
                this.secondaryMetrics(
                        "{\"cpu_core/instructions/:u\":{\"score\":3.0,\"scoreUnit\":\"#/op\"},"
                                + "\"cpu_atom/instructions/:u\":{\"score\":4.0,\"scoreUnit\":\"#/op\"}}"
                )
        );
        Assertions.assertTrue(instructions.isPresent(), "Hybrid instructions metrics with trailing slash should parse");
        Assertions.assertEquals(
                7.0d,
                instructions.orElseThrow().value(),
                1.0e-12,
                "Hybrid counters with trailing slash should be summed"
        );
    }

    @Test
    void omitsInstructionsWhenCountersAreMissing() throws Exception {
        final Optional<JMHInstructions> instructions = PerfInstructionsEvent.metric(
                this.secondaryMetrics("{\"branches:u\":{\"score\":3.0,\"scoreUnit\":\"#/op\"}}")
        );
        Assertions.assertTrue(instructions.isEmpty(), "Missing instructions counters should stay absent");
    }

    private PerfSecondaryMetrics secondaryMetrics(final String json) throws Exception {
        return new PerfSecondaryMetrics(new ObjectMapper().readTree(json));
    }
}
