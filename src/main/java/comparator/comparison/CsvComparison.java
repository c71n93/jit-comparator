package comparator.comparison;

import comparator.Analysis;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CSV comparison table for the original analysis and its refactorings,
 * including the JIT artifacts mean and max dissimilarity scores.
 */
public class CsvComparison {
    private final Analysis original;
    private final List<Analysis> refactorings;

    /**
     * Ctor.
     *
     * @param original
     *            original analysis
     * @param refactorings
     *            analyses for refactored variants
     */
    public CsvComparison(final Analysis original, final Analysis... refactorings) {
        this(original, Arrays.asList(refactorings));
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
        this.original = original;
        this.refactorings = List.copyOf(refactorings);
    }

    /**
     * CSV representation for the original analysis and its refactorings.
     *
     * @return CSV content
     */
    public String asCsv() {
        final StringBuilder csv = new StringBuilder(this.rowToCsv(this.headerCsv()));
        csv.append(System.lineSeparator());
        csv.append(this.rowToCsv(this.rowWith(this.original.asCsvRow(), "Original", "Original")));
        for (final Analysis refactoring : this.refactorings) {
            final JITResultsComparison comparison = new JITResultsComparison(
                    this.original.results(), refactoring.results()
            );
            csv.append(System.lineSeparator());
            csv.append(
                    this.rowToCsv(
                            this.rowWith(
                                    refactoring.asCsvRow(),
                                    String.valueOf(comparison.meanRelativeDifference()),
                                    String.valueOf(comparison.maxRelativeDifference())
                            )
                    )
            );
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
        return this.rowWith(
                this.original.headerCsv(),
                "JIT artifacts mean dissimilarity score",
                "JIT artifacts max dissimilarity score"
        );
    }

    private List<String> rowWith(final List<String> row, final String... values) {
        final List<String> updated = new ArrayList<>(row);
        updated.addAll(Arrays.asList(values));
        return updated;
    }
}
