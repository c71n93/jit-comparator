package comparator.comparison;

import comparator.Analysis;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CSV comparison table for the original analysis and its refactorings,
 * optionally including the JIT metrics mean and max dissimilarity scores.
 */
public class CsvComparison {
    private static final String ORIGINAL = "Original";
    private static final Logger LOG = LoggerFactory.getLogger(CsvComparison.class);
    private final Analysis original;
    private final List<Analysis> refactorings;
    private final boolean compareJitResults;

    /**
     * Ctor.
     *
     * @param original
     *            original analysis
     * @param refactorings
     *            analyses for refactored variants
     */
    public CsvComparison(final Analysis original, final Analysis... refactorings) {
        this(true, original, refactorings);
    }

    /**
     * Ctor.
     *
     * @param compareJitResults
     *            JIT results comparison enabled flag
     * @param original
     *            original analysis
     * @param refactorings
     *            analyses for refactored variants
     */
    public CsvComparison(final boolean compareJitResults, final Analysis original, final Analysis... refactorings) {
        this(compareJitResults, original, Arrays.asList(refactorings));
    }

    /**
     * Ctor.
     *
     * @param original
     *            original analysis
     * @param refactorings
     *            analyses for refactored variants
     */
    public CsvComparison(final Analysis original, final List<Analysis> refactorings) {
        this(true, original, refactorings);
    }

    /**
     * Ctor.
     *
     * @param compareJitResults
     *            JIT results comparison enabled flag
     * @param original
     *            original analysis
     * @param refactorings
     *            analyses for refactored variants
     */
    public CsvComparison(final boolean compareJitResults, final Analysis original,
            final List<Analysis> refactorings) {
        this.original = original;
        this.refactorings = List.copyOf(refactorings);
        this.compareJitResults = compareJitResults;
    }

    /**
     * CSV representation for the original analysis and its refactorings.
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
        final int total = 1 + this.refactorings.size();
        int index = 1;
        CsvComparison.LOG.info("Start writing CSV rows, total: {}", total);
        this.writeRow(writer, this.headerCsv(), 0, total);
        this.writeNextRow(
                writer,
                this.originalRow(),
                index,
                total
        );
        index += 1;
        for (final Analysis refactoring : this.refactorings) {
            this.writeNextRow(
                    writer,
                    this.refactoringRow(refactoring),
                    index,
                    total
            );
            index += 1;
        }
    }

    private void writeRow(final BufferedWriter writer, final List<String> row, final int index,
            final int total) throws IOException {
        if (index > 0) {
            CsvComparison.LOG.info("Writing row {}/{}", index, total);
        }
        writer.write(this.rowToCsv(row));
        writer.flush();
    }

    private void writeNextRow(final BufferedWriter writer, final List<String> row, final int index,
            final int total) throws IOException {
        writer.newLine();
        this.writeRow(writer, row, index, total);
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

    private List<String> headerCsv() {
        if (!this.compareJitResults) {
            return this.original.headerCsv();
        }
        return this.rowWith(
                this.original.headerCsv(),
                "JIT metrics mean dissimilarity score",
                "JIT metrics max dissimilarity score"
        );
    }

    private List<String> originalRow() {
        if (!this.compareJitResults) {
            return this.original.asCsvRow();
        }
        return this.rowWith(
                this.original.asCsvRow(),
                CsvComparison.ORIGINAL,
                CsvComparison.ORIGINAL
        );
    }

    private List<String> refactoringRow(final Analysis refactoring) {
        if (!this.compareJitResults) {
            return refactoring.asCsvRow();
        }
        final JITResultsComparison comparison = new JITResultsComparison(
                this.original.results(),
                refactoring.results()
        );
        return this.rowWith(
                refactoring.asCsvRow(),
                String.valueOf(comparison.meanRelativeDifference()),
                String.valueOf(comparison.maxRelativeDifference())
        );
    }

    private List<String> rowWith(final List<String> row, final String... values) {
        final List<String> updated = new ArrayList<>(row);
        updated.addAll(Arrays.asList(values));
        return updated;
    }
}
