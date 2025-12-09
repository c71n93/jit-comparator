package comparator;

import comparator.jitlog.NativeCodeSize;
import comparator.warmup.RunResult;
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

        final RunResult resultPlain = new WarmupRun(targetPlain).run();
        final int nativeSizePlain = new NativeCodeSize(targetPlain, resultPlain.log()).value();
        System.out.println("Plain tier 4 native code size: " + nativeSizePlain + " bytes");

        final RunResult resultStream = new WarmupRun(targetStream).run();
        final int nativeSizeStream = new NativeCodeSize(targetStream, resultStream.log()).value();
        System.out.println("Stream tier 4 native code size: " + nativeSizeStream + " bytes");
    }
}
