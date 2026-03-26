package comparator.jmh.launch.output.perf;

import com.fasterxml.jackson.databind.JsonNode;

import comparator.jmh.results.JMHInstructions;
import comparator.jmh.results.JMHMemoryLoads;
import comparator.jmh.results.JMHMemoryStores;
import comparator.jmh.results.JMHPerfResults;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parser of perf-profiler metrics inside JMH secondary metrics JSON.
 */
public final class PerfResultFromJSON {
    private static final String INSTRUCTIONS_EVENT = "instructions";
    private static final String HYBRID_INSTRUCTIONS_SUFFIX = "/instructions";
    private static final Logger LOG = LoggerFactory.getLogger(PerfResultFromJSON.class);
    private final PerfSecondaryMetrics secondaryMetrics;
    private final Path source;
    private final boolean perfEnabled;
    private final PerfMemoryEvents.MemoryEvents memoryEvents;
    private final boolean memoryMetricsAvailable;

    public PerfResultFromJSON(final JsonNode secondaryMetrics, final Path source, final boolean perfEnabled) {
        this(
                new PerfSecondaryMetrics(secondaryMetrics),
                source,
                perfEnabled,
                PerfMemoryEvents.events(),
                PerfMemoryEvents.memEventsAvailable()
        );
    }

    PerfResultFromJSON(final PerfSecondaryMetrics secondaryMetrics, final Path source, final boolean perfEnabled,
            final PerfMemoryEvents.MemoryEvents memoryEvents, final boolean memoryMetricsAvailable) {
        this.secondaryMetrics = secondaryMetrics;
        this.source = source;
        this.perfEnabled = perfEnabled;
        this.memoryEvents = memoryEvents;
        this.memoryMetricsAvailable = memoryMetricsAvailable;
    }

    public JMHPerfResults parsedResult() {
        if (!this.perfEnabled) {
            return JMHPerfResults.absent();
        }
        final Optional<JMHInstructions> instructions = this.instructions();
        if (instructions.isEmpty()) {
            PerfResultFromJSON.LOG.warn(
                    "Perf instruction counters are unavailable in the JMH output; perf metrics are skipped."
            );
            return JMHPerfResults.absent();
        }
        final JMHInstructions presentInstructions = instructions.orElseThrow();
        if (!this.memoryMetricsAvailable) {
            PerfResultFromJSON.LOG.warn(
                    "Perf memory events for memory loads and stores are unavailable on this CPU; memory metrics are skipped."
            );
            return JMHPerfResults.from(presentInstructions);
        }
        return JMHPerfResults.from(presentInstructions, this.memoryLoads(), this.memoryStores());
    }

    private Optional<JMHInstructions> instructions() {
        final Optional<PerfMetric> direct = this.secondaryMetrics.metric(PerfResultFromJSON.INSTRUCTIONS_EVENT);
        if (direct.isPresent()) {
            return direct.map(PerfResultFromJSON::instructionsMetric);
        }
        return this.secondaryMetrics.summedMetric(
                PerfResultFromJSON.hybridInstructions()
        ).map(PerfResultFromJSON::instructionsMetric);
    }

    private JMHMemoryLoads memoryLoads() {
        final PerfMetric metric = this.requiredMetric(
                this.memoryEvents.loadMetricNames(),
                this.memoryEvents.loadEventName()
        );
        return new JMHMemoryLoads(metric.score(), metric.unit());
    }

    private JMHMemoryStores memoryStores() {
        final PerfMetric metric = this.requiredMetric(
                this.memoryEvents.storeMetricNames(),
                this.memoryEvents.storeEventName()
        );
        return new JMHMemoryStores(metric.score(), metric.unit());
    }

    private PerfMetric requiredMetric(final List<String> names, final String name) {
        return this.secondaryMetrics.metric(names).orElseThrow(
                () -> new IllegalStateException("Missing " + name + " metric in JMH result file: " + this.source)
        );
    }

    private static JMHInstructions instructionsMetric(final PerfMetric metric) {
        return new JMHInstructions(metric.score(), metric.unit());
    }

    private static Predicate<String> hybridInstructions() {
        return baseName -> baseName.endsWith(PerfResultFromJSON.HYBRID_INSTRUCTIONS_SUFFIX);
    }
}
