package comparator.comparison;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * CSV table for multiple comparisons with concatenated rows.
 */
public class CsvComparisons {
    private final List<CsvComparison> tables;

    /**
     * Ctor.
     *
     * @param comparisons
     *            comparison tables to concatenate
     */
    public CsvComparisons(final CsvComparison... comparisons) {
        this(Arrays.asList(comparisons));
    }

    /**
     * Ctor.
     *
     * @param comparisons
     *            comparison tables to concatenate
     */
    public CsvComparisons(final List<CsvComparison> comparisons) {
        this.tables = List.copyOf(comparisons);
    }

    /**
     * CSV representation for concatenated comparisons.
     *
     * @return CSV content
     */
    public String asCsv() {
        final String lineSeparator = System.lineSeparator();
        final StringBuilder csv = new StringBuilder();
        for (final CsvComparison comparison : this.tables) {
            final String content = comparison.asCsv();
            if (!csv.isEmpty()) {
                csv.append(lineSeparator);
            }
            csv.append(content);
        }
        return csv.toString();
    }

    /**
     * Converts itself to a CSV representation and writes it to a file.
     *
     * @param file
     *            output file path
     */
    public void saveAsCsv(final Path file) {
        try {
            Files.writeString(file, this.asCsv(), StandardCharsets.UTF_8);
        } catch (final IOException exception) {
            throw new IllegalStateException("Unable to write csv to " + file, exception);
        }
    }
}
