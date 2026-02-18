package comparator.jmh.launch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import comparator.jmh.JMHAllocRateNorm;
import comparator.jmh.JMHInstructions;
import comparator.jmh.JMHMemoryLoads;
import comparator.jmh.JMHPrimaryScore;
import comparator.jmh.JMHResults;
import comparator.property.JvmSystemProperties;
import comparator.property.PropertyString;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Represents a JMH result file produced by the JMH JVM. It builds the JVM
 * property string used to pass the file path to the forked process and parses
 * the result file into {@link JMHResults}.
 */
public class JMHResultFile implements JvmSystemProperties {
    private static final PropertyString JMH_RESULT_PROP = new PropertyString("jmh.result.file");
    private static final String GC_ALLOC_RATE_NORM = "gc.alloc.rate.norm";
    private static final String INSTRUCTIONS = "instructions:u";
    private static final String MEMORY_LOADS = "mem_inst_retired.all_loads:u";
    private static final String SCORE_FIELD = "score";
    private static final String SCORE_UNIT_FIELD = "scoreUnit";
    private final Path result;

    public JMHResultFile(final Path result) {
        this.result = result;
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
            final Optional<JMHInstructions> instructions = this.instructionsFrom(secondaryMetrics);
            final Optional<JMHMemoryLoads> memoryLoads = this.memoryLoadsFrom(secondaryMetrics);
            return new JMHResults(score, allocRateNorm, instructions, memoryLoads);
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to read JMH result file", e);
        }
    }

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

    private Optional<JMHInstructions> instructionsFrom(final JsonNode node) {
        final JsonNode instructions = node.path(INSTRUCTIONS);
        if (instructions.isMissingNode() || !instructions.hasNonNull(SCORE_FIELD)
                || !instructions.hasNonNull(SCORE_UNIT_FIELD)) {
            return Optional.empty();
        }
        return Optional.of(
                new JMHInstructions(
                        instructions.get(SCORE_FIELD).asDouble(), instructions.get(SCORE_UNIT_FIELD).asText()
                )
        );
    }

    private Optional<JMHMemoryLoads> memoryLoadsFrom(final JsonNode node) {
        final JsonNode loads = node.path(MEMORY_LOADS);
        if (loads.isMissingNode() || !loads.hasNonNull(SCORE_FIELD) || !loads.hasNonNull(SCORE_UNIT_FIELD)) {
            return Optional.empty();
        }
        return Optional.of(new JMHMemoryLoads(loads.get(SCORE_FIELD).asDouble(), loads.get(SCORE_UNIT_FIELD).asText()));
    }
}
