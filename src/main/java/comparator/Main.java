package comparator;

import comparator.jitlog.NativeCodeSize;
import comparator.warmup.WarmupResult;
import comparator.method.TargetMethod;
import comparator.warmup.WarmupRun;
import java.nio.file.Path;

public class Main {
    @SuppressWarnings({"PMD.AvoidLiteralsInIfCondition", "PMD.SystemPrintln"})
    public static void main(final String[] args) {
        // TODO: this main is temporary, just for test.
        final Path classpath = Path.of("/Users/c71n93/Programming/Diploma/comparator/examples");
        final TargetMethod targetPlain = new TargetMethod(classpath, "PlainForExample", "runFor");
        final TargetMethod targetStream = new TargetMethod(classpath, "StreamBoxedExample", "runStreamBoxed");

        final WarmupResult resultPlain = new WarmupRun(targetPlain).run();
        resultPlain.print(System.out);
        final int nativeSizePlain = new NativeCodeSize(targetPlain, resultPlain.log()).value();
        System.out.println("Plain tier 4 native code size: " + nativeSizePlain + " bytes");

        final WarmupResult resultStream = new WarmupRun(targetStream).run();
        resultStream.print(System.out);
        final int nativeSizeStream = new NativeCodeSize(targetStream, resultStream.log()).value();
        System.out.println("Stream tier 4 native code size: " + nativeSizeStream + " bytes");
    }
}
