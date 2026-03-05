package comparator.comparison;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
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
        final StringWriter buffer = new StringWriter();
        try (BufferedWriter writer = new BufferedWriter(buffer)) {
            this.writeCsvTo(writer);
        } catch (final IOException exception) {
            throw new IllegalStateException("Unable to build csv content", exception);
        }
        return buffer.toString();
    }

    /**
     * Converts itself to a CSV representation and writes it to a file.
     *
     * @param file
     *            output file path
     */
    public void saveAsCsv(final Path file) {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            this.writeCsvTo(writer);
        } catch (final IOException exception) {
            throw new IllegalStateException("Unable to write csv to " + file, exception);
        }
    }

    /**
     * CSV rows written to the provided writer.
     *
     * @param writer
     *            output writer
     * @throws IOException
     *             if write fails
     */
    public void writeCsvTo(final BufferedWriter writer) throws IOException {
        boolean first = true;
        for (final CsvComparison comparison : this.tables) {
            if (!first) {
                writer.newLine();
            }
            comparison.writeCsvTo(writer);
            first = false;
        }
    }
}
