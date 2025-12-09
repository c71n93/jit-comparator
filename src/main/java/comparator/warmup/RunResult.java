package comparator.warmup;

import comparator.Result;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Stores the result of the forked warmup JVM run: JIT log location, exit code
 * and console output. Keeping it simple makes it easy to extend later.
 */
public final class RunResult implements Result {
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

    @Override
    public void print(final OutputStream out) {
        final PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
        writer.println("Warmup JVM exited with code " + this.exitCode);
        writer.println("JIT log stored at " + this.logPath);
        if (!this.stdout.isBlank()) {
            writer.println("--- child stdout ---");
            writer.println(this.stdout);
        }
        if (!this.stderr.isBlank()) {
            writer.println("--- child stderr ---");
            writer.println(this.stderr);
        }
        writer.flush();
    }

    public Path log() {
        return this.logPath;
    }
}
