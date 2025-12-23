package comparator.warmup;

import comparator.Results;
import comparator.warmup.jmh.JMHAllocRateNorm;
import comparator.warmup.jmh.JMHPrimaryScore;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Stores the results of the forked warmup JVM run: JIT log location and
 * benchmark metrics.
 */
public final class WarmupResults implements Results {
    private final Path logPath;
    private final JMHPrimaryScore score;
    private final JMHAllocRateNorm allocRateNorm;

    public WarmupResults(final Path logPath, final JMHPrimaryScore score, final JMHAllocRateNorm allocRateNorm) {
        this.logPath = Objects.requireNonNull(logPath);
        this.score = Objects.requireNonNull(score);
        this.allocRateNorm = Objects.requireNonNull(allocRateNorm);
    }

    @Override
    public void print(final OutputStream out) {
        final PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
        writer.println("Warmup results:");
        writer.println("JIT log stored at " + this.logPath);
        writer.println("Benchmark metrics:");
        writer.println("- " + this.score.toString());
        writer.println("- " + this.allocRateNorm.toString());
        writer.flush();
    }

    public Path log() {
        return this.logPath;
    }

    public JMHPrimaryScore score() {
        return this.score;
    }

    public JMHAllocRateNorm allocRateNorm() {
        return this.allocRateNorm;
    }
}
