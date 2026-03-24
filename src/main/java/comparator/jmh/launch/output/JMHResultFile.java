package comparator.jmh.launch.output;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import comparator.jmh.JMHResults;
import comparator.property.JvmSystemProperties;
import comparator.property.PropertyString;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Represents a JMH result file produced by the JMH JVM.
 */
public class JMHResultFile implements JvmSystemProperties {
    private static final PropertyString JMH_RESULT_PROP = new PropertyString("jmh.result.file");
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
            return new JMHResultFromJSON(this.resultNode(), this.result, this.perfEnabled).parsedResult();
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to read JMH result file", e);
        }
    }

    @Override
    public String toString() {
        return this.result.toString();
    }

    private JsonNode resultNode() throws IOException {
        final JsonNode root = new ObjectMapper().readTree(Files.readString(this.result, StandardCharsets.UTF_8));
        if (!root.isArray() || root.isEmpty()) {
            throw new IllegalStateException("JMH result file is empty: " + this.result);
        }
        return root.get(0);
    }
}
