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
 * including the JIT artifacts equivalence flag.
 */
public class Comparison {
    // TODO: make JITResults return metric names and their measure units. For
    // example "Allocations, B"
    private static final List<String> HEADER = List.of(
            "Target",
            "JMH primary score, us/op",
            "Allocations, B",
            "Instructions, #/op",
            "Memory loads, #/op",
            "Native code size, B",
            "JIT artifacts equivalent?"
    );
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
    public Comparison(final Analysis original, final Analysis... refactorings) {
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
    public Comparison(final Analysis original, final List<Analysis> refactorings) {
        this.original = original;
        this.refactorings = List.copyOf(refactorings);
    }

    /**
     * CSV representation for the original analysis and its refactorings.
     *
     * @return CSV content
     */
    public String asCsv() {
        final StringBuilder csv = new StringBuilder(this.rowToCsv(Comparison.HEADER));
        csv.append(System.lineSeparator());
        csv.append(this.rowToCsv(Comparison.rowWith(this.original.asRow(), "Original")));
        for (final Analysis refactoring : this.refactorings) {
            csv.append(System.lineSeparator());
            csv.append(
                    this.rowToCsv(
                            Comparison.rowWith(
                                    refactoring.asRow(),
                                    String.valueOf(this.original.results().isSame(refactoring.results()))
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

    private static List<String> rowWith(final List<String> row, final String value) {
        final List<String> updated = new ArrayList<>(row);
        updated.add(value);
        return updated;
    }
}
