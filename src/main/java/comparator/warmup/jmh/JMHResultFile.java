package comparator.warmup.jmh;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a JMH result file produced by the warmup JVM. It builds the JVM
 * property string used to pass the file path to the forked process and parses
 * the result files into {@link JMHScore} entries.
 */
public class JMHResultFile {
    private static final String WARMUP_RESULT = "warmup.result.file";
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
        return "-D" + WARMUP_RESULT + "=" + this.result.toAbsolutePath();
    }

    /**
     * Reads the result file path from JVM properties.
     *
     * @return value of the warmup result file property
     */
    public static String resultFileFromProperty() {
        return Objects.requireNonNull(System.getProperty(WARMUP_RESULT), "Missing property: " + WARMUP_RESULT);
    }

    /**
     * Parses the JSON result file into a list of primary metric scores.
     *
     * @return scores from the JMH result file
     */
    public List<JMHScore> parsedResult() {
        final List<JMHScore> scores = new ArrayList<>();
        if (!Files.exists(this.result)) {
            return scores;
        }
        try {
            final ObjectMapper mapper = new ObjectMapper();
            final JsonNode root = mapper.readTree(Files.readString(this.result, StandardCharsets.UTF_8));
            if (root.isArray()) {
                for (final JsonNode node : root) {
                    final JsonNode primary = node.get("primaryMetric");
                    if (primary != null && node.hasNonNull("benchmark")) {
                        scores.add(
                                new JMHScore(
                                        node.get("benchmark").asText(),
                                        primary.get("score").asDouble(),
                                        primary.get("scoreUnit").asText()
                                )
                        );
                    }
                }
            }
            return scores;
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to read warmup result file", e);
        }
    }
}
