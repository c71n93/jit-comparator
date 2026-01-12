package comparator.warmup;

import comparator.Results;
import comparator.warmup.jmh.JMHAllocRateNorm;
import comparator.warmup.jmh.JMHPrimaryScore;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Stores the results of the forked warmup JVM run: benchmark metrics.
 */
public final class JMHResults implements Results {
    private final JMHPrimaryScore score;
    private final JMHAllocRateNorm allocRateNorm;

    public JMHResults(final JMHPrimaryScore score, final JMHAllocRateNorm allocRateNorm) {
        this.score = Objects.requireNonNull(score);
        this.allocRateNorm = Objects.requireNonNull(allocRateNorm);
    }

    @Override
    public void print(final OutputStream out) {
        final PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
        writer.println("JMH results:");
        writer.println("- " + this.score.toString());
        writer.println("- " + this.allocRateNorm.toString());
        writer.flush();
    }

    public JMHPrimaryScore score() {
        return this.score;
    }

    public JMHAllocRateNorm allocRateNorm() {
        return this.allocRateNorm;
    }
}
