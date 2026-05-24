package comparator;

import comparator.comparison.CsvComparison;
import comparator.comparison.CsvComparisons;
import comparator.method.Classpath;
import comparator.method.TargetMethod;
import java.nio.file.Path;

/** Main. */
public final class Main {
    /** Run method. */
    private static final String RUN_METHOD = "run";

    /** Case root. */
    private static final Path CASE_ROOT = Path.of("reproducer", "cases", "case00_primitive_loop_examples");

    /** Class name. */
    private static final String CLASS_NAME = "PrimitiveLoopExample";

    /** Variants directory. */
    private static final String VARIANTS = "variants";

    private Main() {
        // Intentionally empty.
    }

    /**
     * main.
     *
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        new CsvComparisons(
            new CsvComparison(
                Main.analysis(Main.CASE_ROOT.resolve("baseline"), "baseline"),
                Main.analysis(Main.variant("plain_array"), "plain_array"),
                Main.analysis(Main.variant("indexed_loop"), "indexed_loop"),
                Main.analysis(Main.variant("replace_all"), "replace_all"),
                Main.analysis(Main.variant("stream_boxed"), "stream_boxed")
            )
        ).saveAsCsv(Path.of("comparisons.csv"));
    }

    private static Path variant(final String role) {
        return Main.CASE_ROOT.resolve(Main.VARIANTS).resolve(role);
    }

    private static Analysis analysis(final Path classpath, final String label) {
        return new Analysis(
            new TargetMethod(new Classpath(classpath), Main.CLASS_NAME, Main.RUN_METHOD),
            label
        );
    }
}
