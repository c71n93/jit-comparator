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

    private Main() {
        // Intentionally empty.
    }

    /**
     * main.
     *
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        final Classpath loopComputationsClasspath = new Classpath(Path.of("examples", "loop-computations"));
        new CsvComparisons(
            new CsvComparison(
                new Analysis(
                    new TargetMethod(loopComputationsClasspath, "PlainForExample", Main.RUN_METHOD)
                ),
                new Analysis(
                    new TargetMethod(
                        loopComputationsClasspath, "PlainForPlainArrayExample", Main.RUN_METHOD
                    )
                ),
                new Analysis(
                    new TargetMethod(loopComputationsClasspath, "PlainForIndexedExample", Main.RUN_METHOD)
                ),
                new Analysis(
                    new TargetMethod(
                        loopComputationsClasspath, "PlainForReplaceAllExample", Main.RUN_METHOD
                    )
                ),
                new Analysis(
                    new TargetMethod(loopComputationsClasspath, "StreamBoxedExample", Main.RUN_METHOD)
                )
            )
        ).saveAsCsv(Path.of("comparisons.csv"));
    }
}
