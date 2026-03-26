package comparator.jmh.launch.output;

import com.fasterxml.jackson.databind.JsonNode;

import comparator.jmh.launch.output.perf.PerfResultFromJSON;
import comparator.jmh.results.JMHAllocRateNorm;
import comparator.jmh.results.JMHPrimaryScore;
import comparator.jmh.results.JMHResults;

import java.nio.file.Path;

/**
 * Parser of one benchmark entry inside a JMH result JSON array.
 */
final class JMHResultFromJSON {
    private static final String PRIMARY_METRIC = "primaryMetric";
    private static final String SECONDARY_METRICS = "secondaryMetrics";
    private static final String ALLOC_RATE_NORM = "gc.alloc.rate.norm";
    private static final String SCORE_FIELD = "score";
    private static final String SCORE_UNIT_FIELD = "scoreUnit";
    private final JsonNode result;
    private final Path source;
    private final boolean perfEnabled;

    JMHResultFromJSON(final JsonNode result, final Path source, final boolean perfEnabled) {
        this.result = result;
        this.source = source;
        this.perfEnabled = perfEnabled;
    }

    JMHResults parsedResult() {
        final JsonNode secondaryMetrics = this.result.path(JMHResultFromJSON.SECONDARY_METRICS);
        return new JMHResults(
                this.primaryScore(),
                this.allocRateNorm(secondaryMetrics),
                new PerfResultFromJSON(secondaryMetrics, this.source, this.perfEnabled).parsedResult()
        );
    }

    private JMHPrimaryScore primaryScore() {
        final JsonNode metric = JMHResultFromJSON.requiredMetric(
                this.result.path(JMHResultFromJSON.PRIMARY_METRIC),
                "primary metric",
                this.source
        );
        return new JMHPrimaryScore(
                metric.get(JMHResultFromJSON.SCORE_FIELD).asDouble(),
                metric.get(JMHResultFromJSON.SCORE_UNIT_FIELD).asText()
        );
    }

    private JMHAllocRateNorm allocRateNorm(final JsonNode secondaryMetrics) {
        final JsonNode metric = JMHResultFromJSON.requiredMetric(
                secondaryMetrics.path(JMHResultFromJSON.ALLOC_RATE_NORM),
                JMHResultFromJSON.ALLOC_RATE_NORM,
                this.source
        );
        return new JMHAllocRateNorm(
                metric.get(JMHResultFromJSON.SCORE_FIELD).asDouble(),
                metric.get(JMHResultFromJSON.SCORE_UNIT_FIELD).asText()
        );
    }

    private static JsonNode requiredMetric(final JsonNode metric, final String name, final Path source) {
        if (metric.isMissingNode()
                || !metric.hasNonNull(JMHResultFromJSON.SCORE_FIELD)
                || !metric.hasNonNull(JMHResultFromJSON.SCORE_UNIT_FIELD)) {
            throw new IllegalStateException("Missing " + name + " metric in JMH result file: " + source);
        }
        return metric;
    }
}
