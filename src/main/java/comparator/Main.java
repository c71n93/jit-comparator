package comparator;

import comparator.method.TargetMethod;
import java.nio.file.Path;

public class Main {
    public static void main(final String[] args) {
        // TODO: this main is temporary, just for test.
        final Path classpath = Path.of("/Users/c71n93/Programming/Diploma/comparator/examples"); // TODO: fix absolute path
        final TargetMethod targetPlain = new TargetMethod(classpath, "PlainForExample", "runFor");
        final TargetMethod targetStream = new TargetMethod(classpath, "StreamBoxedExample", "runStreamBoxed");

        new Analysis(targetPlain).results().print(System.out);
        new Analysis(targetStream).results().print(System.out);
    }
}
