package comparator.warmup;

import comparator.method.TargetMethod;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

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

    public RunResult run() {
        try {
            final Process process = new ProcessBuilder(this.command.asList()).start();
            final String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            final String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            final int exitCode = process.waitFor();
            return new RunResult(this.command.logFile(), exitCode, stdout, stderr); // TODO: put benchmark scores
                                                                                    // (execution time) into result.
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Warmup run interrupted", e);
        } catch (final IOException e) {
            throw new IllegalStateException("Warmup run failed", e);
        }
    }
}
