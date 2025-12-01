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

    @SuppressWarnings("PMD.SystemPrintln")
    public void print() {
        System.out.println("Warmup JVM exited with code " + this.exitCode);
        System.out.println("JIT log stored at " + this.logPath);
        if (!this.stdout.isBlank()) {
            System.out.println("--- child stdout ---");
            System.out.println(this.stdout);
        }
        if (!this.stderr.isBlank()) {
            System.err.println("--- child stderr ---");
            System.err.println(this.stderr);
        }
    }
}
