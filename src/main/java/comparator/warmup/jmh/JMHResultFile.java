package comparator.warmup.jmh;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import comparator.warmup.JMHResults;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Represents a JMH result file produced by the warmup JVM. It builds the JVM
 * property string used to pass the file path to the forked process and parses
 * the result file into {@link JMHResults}.
 */
public class JMHResultFile {
    private static final String WARMUP_RESULT_PROP = "warmup.result.file";
    private static final String GC_ALLOC_RATE_NORM = "gc.alloc.rate.norm";
    private static final String SCORE_FIELD = "score";
    private static final String SCORE_UNIT_FIELD = "scoreUnit";
    private final Path result;

    public JMHResultFile(final Path result) {
        this.result = result;
    }

    /**
     * Builds the JVM property string that points to the result file path.
     *
     * @return JVM property in {@code -Dname=value} form
     */
    public String property() {
        return "-D" + WARMUP_RESULT_PROP + "=" + this.result.toAbsolutePath();
    }

    /**
     * Reads the result file path from JVM properties.
     *
     * @return value of the warmup result file property
     */
    public static String resultFileFromProperty() {
        return Objects
                .requireNonNull(System.getProperty(WARMUP_RESULT_PROP), "Missing property: " + WARMUP_RESULT_PROP);
    }

    /**
     * Parses the JSON result file into warmup results.
     *
     * @return results from the JMH result file
     */
    public JMHResults parsedResult() {
        if (!Files.exists(this.result)) {
            throw new IllegalStateException("Warmup result file is missing: " + this.result);
        }
        try {
            final ObjectMapper mapper = new ObjectMapper();
            final JsonNode root = mapper.readTree(Files.readString(this.result, StandardCharsets.UTF_8));
            if (!root.isArray() || root.isEmpty()) {
                throw new IllegalStateException("Warmup result file is empty: " + this.result);
            }
            final JsonNode node = root.get(0);
            final JMHPrimaryScore score = this.scoreFrom(node.path("primaryMetric"));
            final JMHAllocRateNorm allocRateNorm = this.allocRateNormFrom(
                    node.path("secondaryMetrics").path(GC_ALLOC_RATE_NORM)
            );
            return new JMHResults(score, allocRateNorm);
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to read warmup result file", e);
        }
    }

    private JMHPrimaryScore scoreFrom(final JsonNode node) {
        if (node.isMissingNode() || !node.hasNonNull(SCORE_FIELD) || !node.hasNonNull(SCORE_UNIT_FIELD)) {
            throw new IllegalStateException("Missing primary metric in warmup result file: " + this.result);
        }
        return new JMHPrimaryScore(node.get(SCORE_FIELD).asDouble(), node.get(SCORE_UNIT_FIELD).asText());
    }

    private JMHAllocRateNorm allocRateNormFrom(final JsonNode node) {
        if (node.isMissingNode() || !node.hasNonNull(SCORE_FIELD) || !node.hasNonNull(SCORE_UNIT_FIELD)) {
            throw new IllegalStateException("Missing gc.alloc.rate.norm in warmup result file: " + this.result);
        }
        return new JMHAllocRateNorm(node.get(SCORE_FIELD).asDouble(), node.get(SCORE_UNIT_FIELD).asText());
    }
}
