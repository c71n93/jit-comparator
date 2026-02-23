package comparator;

import comparator.comparison.CsvComparison;
import comparator.comparison.CsvComparisons;
import comparator.method.Classpath;
import comparator.method.TargetMethod;
import java.nio.file.Path;

public class Main {
    private static final String RUN_METHOD = "run";

    public static void main(final String[] args) {
        final Classpath loopComputationsClasspath = new Classpath(Path.of("examples", "loop-computations"));
        final Path jitlog = Path.of("jit-log.xml");
        new CsvComparisons(
                new CsvComparison(
                        new Analysis(
                                new TargetMethod(loopComputationsClasspath, "PlainForExample", Main.RUN_METHOD), jitlog
                        ),
                        new Analysis(
                                new TargetMethod(
                                        loopComputationsClasspath, "PlainForPlainArrayExample", Main.RUN_METHOD
                                ),
                                jitlog
                        ),
                        new Analysis(
                                new TargetMethod(loopComputationsClasspath, "PlainForIndexedExample", Main.RUN_METHOD),
                                jitlog
                        ),
                        new Analysis(
                                new TargetMethod(
                                        loopComputationsClasspath, "PlainForReplaceAllExample", Main.RUN_METHOD
                                ),
                                jitlog
                        ),
                        new Analysis(
                                new TargetMethod(loopComputationsClasspath, "StreamBoxedExample", Main.RUN_METHOD),
                                jitlog
                        )
                )
        ).saveAsCsv(Path.of("comparisons.csv"));
    }
}
