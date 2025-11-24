package c71n93;

import c71n93.warmup.TargetMethod;
import c71n93.warmup.WarmupRun;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        // TODO: use some conventional library to parse arguments
        if (args.length != 3) {
            System.err.println("Usage: java c71n93.Main <classpath-entry> <class-name> <method-name>");
            System.exit(1);
        }
        new WarmupRun(
            new TargetMethod(Path.of(args[0]), args[1], args[2])
        ).run().print();
    }
}
