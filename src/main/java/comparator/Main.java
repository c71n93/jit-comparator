package comparator;

import comparator.comparison.AnalysesToCsv;
import comparator.method.TargetMethod;
import java.nio.file.Path;

public class Main {
    public static void main(final String[] args) {
        final Path forVsStreamClasspath = Path.of("examples", "for-vs-stream");
        new AnalysesToCsv(
                new Analysis(new TargetMethod(forVsStreamClasspath, "PlainForExample", "runFor")),
                new Analysis(new TargetMethod(forVsStreamClasspath, "StreamBoxedExample", "runStreamBoxed"))
        ).save(Path.of("for_vs_stream.csv"));

        final Path arrayVsListClasspath = Path.of("examples", "array-vs-list");
        new AnalysesToCsv(
                new Analysis(new TargetMethod(arrayVsListClasspath, "ArrayExample", "runArray")),
                new Analysis(new TargetMethod(arrayVsListClasspath, "ArrayListExample", "runArrayList"))
        ).save(Path.of("array_vs_list.csv"));
    }
}
