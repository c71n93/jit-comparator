package comparator.comparison;

import comparator.Analysis;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CSV table for a collection of analyses.
 */
public class AnalysesToCsv {
    private static final List<String> HEADER = List.of(
            "Target",
            "JMH primary score, us/op",
            "Allocations, B",
            "Native code size, B"
    );
    private final List<Analysis> analyses;

    /**
     * Ctor.
     *
     * @param analyses
     *            analyses for conversion
     */
    // TODO: accept analyses as pairs of original + refactored.
    public AnalysesToCsv(final Analysis... analyses) {
        this(Arrays.asList(analyses));
    }

    /**
     * Ctor.
     *
     * @param analyses
     *            analyses for conversion
     */
    public AnalysesToCsv(final List<Analysis> analyses) {
        this.analyses = List.copyOf(analyses);
    }

    /**
     * Returns the CSV representation for the analyses.
     *
     * @return CSV content
     */
    // TODO: add comparison of results (call of JITResults.isSame) to the row.
    public String value() {
        final StringBuilder csv = new StringBuilder(this.rowToCsv(AnalysesToCsv.HEADER));
        for (final Analysis analysis : this.analyses) {
            csv.append(System.lineSeparator());
            csv.append(this.rowToCsv(analysis.asRow()));
        }
        return csv.toString();
    }

    /**
     * Writes the CSV representation to a file.
     *
     * @param file
     *            output file path
     */
    public void save(final Path file) {
        try {
            Files.writeString(file, this.value(), StandardCharsets.UTF_8);
        } catch (final IOException exception) {
            throw new IllegalStateException("Unable to write csv to " + file, exception);
        }
    }

    private String rowToCsv(final List<String> row) {
        return row.stream().map(this::escape).collect(Collectors.joining(","));
    }

    private String escape(final String value) {
        final boolean quote = value.contains(",") || value.contains("\"") || value.contains("\n")
                || value.contains("\r");
        if (!quote) {
            return value;
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
