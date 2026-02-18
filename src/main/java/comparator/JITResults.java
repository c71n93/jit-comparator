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
                && this.instructionsAreSame(other);
    }

    private boolean instructionsAreSame(final JITResults other) {
        if (this.jmh.instructions().isPresent() && other.jmh.instructions().isPresent()) {
            return this.jmh.instructions().orElseThrow().isSame(other.jmh.instructions().orElseThrow());
        }
        return this.jmh.instructions().isEmpty() && other.jmh.instructions().isEmpty();
    }
}
