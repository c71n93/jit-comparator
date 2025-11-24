package c71n93.warmup;

import java.nio.file.Path;

/**
 * Stores the result of the forked warmup JVM run: JIT log location, exit code
 * and console output. Keeping it simple makes it easy to extend later.
 */
public final class RunResult {
    private final Path logPath;
    private final int exitCode;
    private final String stdout;
    private final String stderr;

    public RunResult(final Path logPath, final int exitCode, final String stdout, final String stderr) {
        this.logPath = logPath;
        this.exitCode = exitCode;
        this.stdout = stdout;
        this.stderr = stderr;
    }

    public void print() {
        System.out.println("Warmup JVM exited with code " + exitCode);
        System.out.println("JIT log stored at " + logPath);
        if (!stdout.isBlank()) {
            System.out.println("--- child stdout ---");
            System.out.println(stdout);
        }
        if (!stderr.isBlank()) {
            System.err.println("--- child stderr ---");
            System.err.println(stderr);
        }
    }
}
