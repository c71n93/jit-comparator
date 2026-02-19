package comparator;

import comparator.jitlog.LogResults;
import comparator.jmh.JMHResults;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * The results of analyzing various JIT artifacts of a specific target method.
 * Combines all other results.
 */
public class JITResults implements Results {
    private final JMHResults jmh;
    private final LogResults jitlog;

    public JITResults(final JMHResults jmh, final LogResults jitlog) {
        this.jmh = jmh;
        this.jitlog = jitlog;
    }

    @Override
    public void print(final OutputStream out) {
        final PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
        writer.println("JIT results:");
        this.jmh.print(out);
        this.jitlog.print(out);
        writer.println("---------");
    }

    @Override
    public List<String> asRow() {
        final List<String> row = new ArrayList<>();
        row.addAll(this.jmh.asRow());
        row.addAll(this.jitlog.asRow());
        return row;
    }

    /**
     * Compares this result set with another using artifact-level equivalence in a
     * priority order.
     *
     * @param other
     *            other results to compare
     * @return {@code true} if all tracked artifacts are considered equivalent
     */
    // TODO: Temporary implementation. Metrics priority order should be verified.
    public boolean isSame(final JITResults other) {
        return this.jmh.primaryScore().isSame(other.jmh.primaryScore())
                && this.jitlog.codesize().isSame(other.jitlog.codesize())
                && this.jmh.allocRateNorm().isSame(other.jmh.allocRateNorm())
                && this.instructionsAreSame(other)
                && this.memoryLoadsAreSame(other);
    }

    // TODO: Implement some toVector method (similar to asRow, but for values) to
    // make this operations in a loop.
    public double relativeDifference(final JITResults other) {
        double sumSquares = 0.0d;
        int count = 0;
        final double primaryScore = this.jmh.primaryScore().relativeDifference(other.jmh.primaryScore());
        sumSquares += primaryScore * primaryScore;
        count += 1;
        final double allocRate = this.jmh.allocRateNorm().relativeDifference(other.jmh.allocRateNorm());
        sumSquares += allocRate * allocRate;
        count += 1;
        final double codeSize = this.jitlog.codesize().relativeDifference(other.jitlog.codesize());
        sumSquares += codeSize * codeSize;
        count += 1;
        if (this.jmh.instructions().isPresent() && other.jmh.instructions().isPresent()) {
            final double instructions = this.jmh.instructions().orElseThrow()
                    .relativeDifference(other.jmh.instructions().orElseThrow());
            sumSquares += instructions * instructions;
            count += 1;
        }
        if (this.jmh.memoryLoads().isPresent() && other.jmh.memoryLoads().isPresent()) {
            final double memoryLoads = this.jmh.memoryLoads().orElseThrow()
                    .relativeDifference(other.jmh.memoryLoads().orElseThrow());
            sumSquares += memoryLoads * memoryLoads;
            count += 1;
        }
        if (count == 0) {
            return 0.0d;
        }
        return Math.sqrt(sumSquares / count);
    }

    private boolean instructionsAreSame(final JITResults other) {
        if (this.jmh.instructions().isPresent() && other.jmh.instructions().isPresent()) {
            return this.jmh.instructions().orElseThrow().isSame(other.jmh.instructions().orElseThrow());
        }
        return this.jmh.instructions().isEmpty() && other.jmh.instructions().isEmpty();
    }

    private boolean memoryLoadsAreSame(final JITResults other) {
        if (this.jmh.memoryLoads().isPresent() && other.jmh.memoryLoads().isPresent()) {
            return this.jmh.memoryLoads().orElseThrow().isSame(other.jmh.memoryLoads().orElseThrow());
        }
        return this.jmh.memoryLoads().isEmpty() && other.jmh.memoryLoads().isEmpty();
    }
}
