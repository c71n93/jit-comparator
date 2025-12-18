package comparator.warmup;

import comparator.method.TargetMethod;
import comparator.warmup.jmh.JMHScore;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Launcher that spawns a separate JVM with JIT logging enabled and asks a tiny
 * JMH benchmark to warm up the target method. The fork is needed because
 * {@code -XX:+LogCompilation} can only be provided on JVM startup.
 */
public class WarmupRun {
    private final WarmupCommand command;

    public WarmupRun(final TargetMethod targetMethod) {
        this(new WarmupCommand(targetMethod));
    }

    public WarmupRun(final WarmupCommand command) {
        this.command = command;
    }

    public WarmupResults run() {
        try {
            final Process process = new ProcessBuilder(this.command.asList()).start();
            final String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            final String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            final int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IllegalStateException(
                        "Warmup run failed with exit code " + exitCode + "\nstdout:\n" + stdout + "\nstderr:\n" + stderr
                );
            }
            final List<JMHScore> scores = this.command.resultFile().parsedResult();
            return new WarmupResults(this.command.logFile(), scores);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Warmup run interrupted", e);
        } catch (final IOException e) {
            throw new IllegalStateException("Warmup run failed", e);
        }
    }
}
