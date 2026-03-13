package comparator.jmh.launch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import comparator.jmh.JMHAllocRateNorm;
import comparator.jmh.JMHInstructions;
import comparator.jmh.JMHMemoryLoads;
import comparator.jmh.JMHMemoryStores;
import comparator.jmh.JMHPerfResults;
import comparator.jmh.JMHPrimaryScore;
import comparator.jmh.JMHResults;
import comparator.jmh.perf.PerfMemoryEvents;
import comparator.property.JvmSystemProperties;
import comparator.property.PropertyString;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Represents a JMH result file produced by the JMH JVM. It builds the JVM
 * property string used to pass the file path to the forked process and parses
 * the result file into {@link JMHResults}.
 */
public class JMHResultFile implements JvmSystemProperties {
    private static final PropertyString JMH_RESULT_PROP = new PropertyString("jmh.result.file");
    private static final String GC_ALLOC_RATE_NORM = "gc.alloc.rate.norm";
    private static final String INSTRUCTIONS = "instructions";
    private static final char PERF_METRIC_SUFFIX_SEPARATOR = ':';
    private static final String SCORE_FIELD = "score";
    private static final String SCORE_UNIT_FIELD = "scoreUnit";
    private final Path result;
    private final boolean perfEnabled;

    public JMHResultFile(final Path result, final boolean perfEnabled) {
        this.result = result;
        this.perfEnabled = perfEnabled;
    }

    @Override
    public List<String> asJvmPropertyArgs() {
        return List.of(JMHResultFile.JMH_RESULT_PROP.asJvmArg(this.result.toAbsolutePath().toString()));
    }

    /**
     * Reads the result file path from JVM system property arguments.
     *
     * @return value of the JMH result file property
     */
    public static String resultFileFromProperty() {
        return JMHResultFile.JMH_RESULT_PROP.requireValue();
    }

    /**
     * Parses the JSON result file into JMH results.
     *
     * @return results from the JMH result file
     */
    public JMHResults parsedResult() {
        if (!Files.exists(this.result)) {
            throw new IllegalStateException("JMH result file is missing: " + this.result);
        }
        try {
            final JsonNode root = new ObjectMapper().readTree(Files.readString(this.result, StandardCharsets.UTF_8));
            if (!root.isArray() || root.isEmpty()) {
                throw new IllegalStateException("JMH result file is empty: " + this.result);
            }
            final JsonNode node = root.get(0);
            final JsonNode secondaryMetrics = node.path("secondaryMetrics");
            final JMHPrimaryScore score = this.scoreFrom(node.path("primaryMetric"));
            final JMHAllocRateNorm allocRateNorm = this.allocRateNormFrom(secondaryMetrics.path(GC_ALLOC_RATE_NORM));
            return new JMHResults(score, allocRateNorm, this.perfResultsFrom(secondaryMetrics));
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to read JMH result file", e);
        }
    }

    @Override
    public String toString() {
        return this.result.toString();
    }

    // TODO: fix the copy-paste in this set of methods.
    private JMHPrimaryScore scoreFrom(final JsonNode node) {
        if (node.isMissingNode() || !node.hasNonNull(SCORE_FIELD) || !node.hasNonNull(SCORE_UNIT_FIELD)) {
            throw new IllegalStateException("Missing primary metric in JMH result file: " + this.result);
        }
        return new JMHPrimaryScore(node.get(SCORE_FIELD).asDouble(), node.get(SCORE_UNIT_FIELD).asText());
    }

    private JMHAllocRateNorm allocRateNormFrom(final JsonNode node) {
        if (node.isMissingNode() || !node.hasNonNull(SCORE_FIELD) || !node.hasNonNull(SCORE_UNIT_FIELD)) {
            throw new IllegalStateException("Missing gc.alloc.rate.norm in JMH result file: " + this.result);
        }
        return new JMHAllocRateNorm(node.get(SCORE_FIELD).asDouble(), node.get(SCORE_UNIT_FIELD).asText());
    }

    private JMHInstructions instructionsFrom(final JsonNode node) {
        final JsonNode instructions = this.metricFrom(node, JMHResultFile.INSTRUCTIONS);
        if (this.missingMetric(instructions)) {
            throw new IllegalStateException("Missing instructions metric in JMH result file: " + this.result);
        }
        return new JMHInstructions(
                instructions.get(SCORE_FIELD).asDouble(), instructions.get(SCORE_UNIT_FIELD).asText()
        );
    }

    private JMHMemoryLoads memoryLoadsFrom(final JsonNode node) {
        final String memoryLoads = PerfMemoryEvents.events().loadEventName();
        final JsonNode loads = this.metricFrom(node, memoryLoads);
        if (this.missingMetric(loads)) {
            throw new IllegalStateException("Missing " + memoryLoads + " metric in JMH result file: " + this.result);
        }
        return new JMHMemoryLoads(loads.get(SCORE_FIELD).asDouble(), loads.get(SCORE_UNIT_FIELD).asText());
    }

    private JMHMemoryStores memoryStoresFrom(final JsonNode node) {
        final String memoryStores = PerfMemoryEvents.events().storeEventName();
        final JsonNode stores = this.metricFrom(node, memoryStores);
        if (this.missingMetric(stores)) {
            throw new IllegalStateException("Missing " + memoryStores + " metric in JMH result file: " + this.result);
        }
        return new JMHMemoryStores(stores.get(SCORE_FIELD).asDouble(), stores.get(SCORE_UNIT_FIELD).asText());
    }

    private JsonNode metricFrom(final JsonNode secondaryMetrics, final String eventName) {
        final JsonNode exactMetric = secondaryMetrics.path(eventName);
        if (!exactMetric.isMissingNode()) {
            return exactMetric;
        }
        final Iterator<Map.Entry<String, JsonNode>> fields = secondaryMetrics.fields();
        while (fields.hasNext()) {
            final Map.Entry<String, JsonNode> field = fields.next();
            if (this.sameMetricBase(field.getKey(), eventName)) {
                return field.getValue();
            }
        }
        return MissingNode.getInstance();
    }

    private boolean sameMetricBase(final String metricName, final String eventName) {
        final int separator = metricName.indexOf(JMHResultFile.PERF_METRIC_SUFFIX_SEPARATOR);
        return separator > 0 && eventName.equals(metricName.substring(0, separator));
    }

    private boolean missingMetric(final JsonNode metric) {
        return metric.isMissingNode() || !metric.hasNonNull(SCORE_FIELD) || !metric.hasNonNull(SCORE_UNIT_FIELD);
    }

    @SuppressWarnings("PMD.SystemPrintln")
    private JMHPerfResults perfResultsFrom(final JsonNode secondaryMetrics) {
        if (!this.perfEnabled) {
            return JMHPerfResults.absent();
        }
        final JMHInstructions instructions = this.instructionsFrom(secondaryMetrics);
        if (!PerfMemoryEvents.memEventsAvailable()) {
            // TODO: Add logging to the project.
            System.err.println(
                    "WARNING: perf memory events for memory loads and stores counting are unavailable on your CPU; memory metrics will be skipped."
            );
            return JMHPerfResults.from(instructions);
        }
        final JMHMemoryLoads memoryLoads = this.memoryLoadsFrom(secondaryMetrics);
        final JMHMemoryStores memoryStores = this.memoryStoresFrom(secondaryMetrics);
        return JMHPerfResults.from(instructions, memoryLoads, memoryStores);
    }
}
