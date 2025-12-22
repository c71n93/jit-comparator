package comparator;

import comparator.method.TargetMethod;
import java.nio.file.Path;

public class Main {
    public static void main(final String[] args) {
        final Path forVsStreamClasspath = Path.of("examples", "for-vs-stream");
        new Analysis(new TargetMethod(forVsStreamClasspath, "PlainForExample", "runFor")).results().print(System.out);
        new Analysis(new TargetMethod(forVsStreamClasspath, "StreamBoxedExample", "runStreamBoxed")).results().print(System.out);

        final Path arrayVsListClasspath = Path.of("examples", "array-vs-list");
        new Analysis(new TargetMethod(arrayVsListClasspath, "ArrayExample", "runArray")).results().print(System.out);
        new Analysis(new TargetMethod(arrayVsListClasspath, "ArrayListExample", "runArrayList")).results().print(System.out);
    }
}
