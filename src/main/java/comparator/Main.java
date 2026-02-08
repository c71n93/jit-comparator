package comparator;

import comparator.comparison.Comparison;
import comparator.comparison.Comparisons;
import comparator.method.Classpath;
import comparator.method.TargetMethod;
import java.nio.file.Path;

public class Main {
    private static final String RUN_METHOD = "run";

    public static void main(final String[] args) {
        final Classpath loopComputationsClasspath = new Classpath(Path.of("examples", "loop-computations"));
        new Comparisons(
                new Comparison(
                        new Analysis(new TargetMethod(loopComputationsClasspath, "PlainForExample", Main.RUN_METHOD)),
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
                        new Analysis(new TargetMethod(loopComputationsClasspath, "StreamBoxedExample", Main.RUN_METHOD))
                ),
                new Comparison(
                        new Analysis(new TargetMethod(loopComputationsClasspath, "PlainForExample", Main.RUN_METHOD)),
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
                        new Analysis(new TargetMethod(loopComputationsClasspath, "StreamBoxedExample", Main.RUN_METHOD))
                )
        ).saveAsCsv(Path.of("comparisons.csv"));
    }
}
