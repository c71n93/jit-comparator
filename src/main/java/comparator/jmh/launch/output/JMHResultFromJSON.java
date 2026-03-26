package comparator.jmh.launch.output;

import com.fasterxml.jackson.databind.JsonNode;

import comparator.jmh.launch.output.perf.PerfResultFromJSON;
import comparator.jmh.results.JMHAllocRateNorm;
import comparator.jmh.results.JMHAllocRateNormError;
import comparator.jmh.results.JMHAllocRateNormResults;
import comparator.jmh.results.JMHPrimaryScore;
import comparator.jmh.results.JMHPrimaryScoreError;
import comparator.jmh.results.JMHPrimaryScoreResults;
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
    private static final String SCORE_ERROR_FIELD = "scoreError";
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

    private JMHPrimaryScoreResults primaryScore() {
        final JsonNode metric = JMHResultFromJSON.requiredMetric(
                this.result.path(JMHResultFromJSON.PRIMARY_METRIC),
                "primary metric",
                this.source
        );
        final double score = metric.get(JMHResultFromJSON.SCORE_FIELD).asDouble();
        return new JMHPrimaryScoreResults(
                new JMHPrimaryScore(score, metric.get(JMHResultFromJSON.SCORE_UNIT_FIELD).asText()),
                new JMHPrimaryScoreError(score, metric.get(JMHResultFromJSON.SCORE_ERROR_FIELD).asDouble())
        );
    }

    private JMHAllocRateNormResults allocRateNorm(final JsonNode secondaryMetrics) {
        final JsonNode metric = JMHResultFromJSON.requiredMetric(
                secondaryMetrics.path(JMHResultFromJSON.ALLOC_RATE_NORM),
                JMHResultFromJSON.ALLOC_RATE_NORM,
                this.source
        );
        final double score = metric.get(JMHResultFromJSON.SCORE_FIELD).asDouble();
        return new JMHAllocRateNormResults(
                new JMHAllocRateNorm(score, metric.get(JMHResultFromJSON.SCORE_UNIT_FIELD).asText()),
                new JMHAllocRateNormError(
                        score,
                        metric.get(JMHResultFromJSON.SCORE_ERROR_FIELD).asDouble()
                )
        );
    }

    private static JsonNode requiredMetric(final JsonNode metric, final String name, final Path source) {
        if (metric.isMissingNode()
                || !metric.hasNonNull(JMHResultFromJSON.SCORE_FIELD)
                || !metric.hasNonNull(JMHResultFromJSON.SCORE_ERROR_FIELD)
                || !metric.hasNonNull(JMHResultFromJSON.SCORE_UNIT_FIELD)) {
            throw new IllegalStateException("Missing " + name + " metric in JMH result file: " + source);
        }
        return metric;
    }
}
