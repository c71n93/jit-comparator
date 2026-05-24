package comparator.reproducer;

import comparator.Analysis;
import comparator.comparison.CsvComparison;
import comparator.method.Classpath;
import comparator.method.TargetMethod;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

/**
 * Batch comparator command line entry point for reproducer manifests.
 */
@SuppressWarnings("PMD.SystemPrintln")
public final class ComparePairs {
    private ComparePairs() {
    }

    /**
     * Main method.
     *
     * @param args
     *            command line arguments
     */
    public static void main(final String[] args) {
        try {
            final ComparePairsOptions options = ComparePairsOptions.fromArgs(args);
            if (options.help()) {
                System.out.println("Usage: ComparePairs --manifest <pairs.csv> --output <comparisons.csv>");
            } else {
                new ComparePairs().run(options);
            }
        } catch (final IllegalArgumentException | IllegalStateException exception) {
            System.err.println(exception.getMessage());
            System.exit(1);
        }
    }

    private void run(final ComparePairsOptions options) {
        try {
            Files.createDirectories(options.output().toAbsolutePath().getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(options.output(), StandardCharsets.UTF_8)) {
                boolean headerWritten = false;
                for (final ManifestCase comparison : ComparisonManifest.fromFile(options.manifest()).cases().values()) {
                    headerWritten = this.writeComparison(comparison, writer, headerWritten);
                }
            }
        } catch (final IOException exception) {
            throw new IllegalStateException("Unable to write comparisons: " + options.output(), exception);
        }
    }

    private boolean writeComparison(final ManifestCase comparison, final BufferedWriter writer,
                                    final boolean headerWritten)
        throws IOException {
        final String csv = new CsvComparison(
            this.analysis(comparison.baseline()),
            comparison.variants().stream().map(this::analysis).toList()
        ).asCsv();
        final List<String> lines = new BufferedReaderLines(csv).asList();
        int start = 0;
        if (headerWritten) {
            start = 1;
        }
        for (int index = start; index < lines.size(); index += 1) {
            if (headerWritten || index > start) {
                writer.newLine();
            }
            writer.write(lines.get(index));
        }
        return true;
    }

    private Analysis analysis(final ManifestEntry entry) {
        return new Analysis(
            new TargetMethod(new Classpath(entry.classpath()), entry.className(), entry.methodName()),
            entry.jitLog(),
            entry.jmhResult(),
            entry.label()
        );
    }

    /**
     * Line list extracted from a generated CSV string.
     */
    private static final class BufferedReaderLines {
        /** CSV content. */
        private final String csv;

        private BufferedReaderLines(final String csv) {
            this.csv = csv;
        }

        private List<String> asList() {
            return new java.io.BufferedReader(new StringReader(this.csv)).lines().toList();
        }
    }
}
