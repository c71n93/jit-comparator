package comparator.jmh.launch.output.perf;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * View of perf-profiler entries inside JMH secondary metrics.
 */
public final class PerfSecondaryMetrics {
    private static final char METRIC_SUFFIX_SEPARATOR = ':';
    private static final char METRIC_PATH_SEPARATOR = '/';
    private static final String SCORE_FIELD = "score";
    private static final String SCORE_UNIT_FIELD = "scoreUnit";
    private static final String DEFAULT_UNIT = "#/op";
    private final JsonNode metrics;

    public PerfSecondaryMetrics(final JsonNode metrics) {
        this.metrics = metrics;
    }

    public Optional<PerfMetric> metric(final String eventName) {
        final Optional<PerfMetric> exact = this.metricFromNode(this.metrics.path(eventName));
        if (exact.isPresent()) {
            return exact;
        }
        final Iterator<Map.Entry<String, JsonNode>> fields = this.metrics.fields();
        while (fields.hasNext()) {
            final Map.Entry<String, JsonNode> field = fields.next();
            if (this.metricBaseName(field.getKey()).equals(eventName)) {
                return this.metricFromNode(field.getValue());
            }
        }
        return Optional.empty();
    }

    public Optional<PerfMetric> summedMetric(final Predicate<String> matcher) {
        double score = 0.0d;
        Optional<String> unit = Optional.empty();
        boolean found = false;
        final Iterator<Map.Entry<String, JsonNode>> fields = this.metrics.fields();
        while (fields.hasNext()) {
            final Map.Entry<String, JsonNode> field = fields.next();
            final String baseName = this.metricBaseName(field.getKey());
            if (!matcher.test(baseName)) {
                continue;
            }
            final Optional<PerfMetric> metric = this.metricFromNode(field.getValue());
            if (metric.isEmpty()) {
                continue;
            }
            final PerfMetric present = metric.orElseThrow();
            found = true;
            score += present.score();
            if (unit.isEmpty()) {
                unit = Optional.of(present.unit());
            }
        }
        if (!found) {
            return Optional.empty();
        }
        return Optional.of(new PerfMetric(score, unit.orElse(PerfSecondaryMetrics.DEFAULT_UNIT)));
    }

    private String metricBaseName(final String metricName) {
        final int separator = metricName.indexOf(PerfSecondaryMetrics.METRIC_SUFFIX_SEPARATOR);
        final String base = separator > 0 ? metricName.substring(0, separator) : metricName;
        return this.trimTrailingPathSeparators(base);
    }

    private String trimTrailingPathSeparators(final String metricName) {
        int endExclusive = metricName.length();
        while (endExclusive > 0
                && metricName.charAt(endExclusive - 1) == PerfSecondaryMetrics.METRIC_PATH_SEPARATOR) {
            endExclusive--;
        }
        return metricName.substring(0, endExclusive);
    }

    private Optional<PerfMetric> metricFromNode(final JsonNode metric) {
        if (metric.isMissingNode() || !metric.hasNonNull(PerfSecondaryMetrics.SCORE_FIELD)) {
            return Optional.empty();
        }
        return Optional.of(
                new PerfMetric(
                        metric.get(PerfSecondaryMetrics.SCORE_FIELD).asDouble(),
                        metric.path(PerfSecondaryMetrics.SCORE_UNIT_FIELD).asText(PerfSecondaryMetrics.DEFAULT_UNIT)
                )
        );
    }
}
