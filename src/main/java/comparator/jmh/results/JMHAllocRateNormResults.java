package comparator.jmh.results;

import comparator.Artifact;
import comparator.Results;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Allocation-rate result pair with the metric value and its relative error.
 */
public final class JMHAllocRateNormResults implements Results {
    private final JMHAllocRateNorm allocRateNorm;
    private final JMHAllocRateNormError error;

    /**
     * Ctor.
     *
     * @param allocRateNorm
     *            allocation-rate metric
     * @param error
     *            allocation-rate relative error
     */
    public JMHAllocRateNormResults(final JMHAllocRateNorm allocRateNorm, final JMHAllocRateNormError error) {
        this.allocRateNorm = allocRateNorm;
        this.error = error;
    }

    /**
     * Result pair without a known relative error value.
     *
     * @param allocRateNorm
     *            allocation-rate metric
     * @return allocation-rate results with missing error represented as
     *         {@link Double#NaN}
     */
    public static JMHAllocRateNormResults withoutError(final JMHAllocRateNorm allocRateNorm) {
        return new JMHAllocRateNormResults(allocRateNorm, new JMHAllocRateNormError(Double.NaN));
    }

    @Override
    public void print(final OutputStream out) {
        final PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
        writer.println("- " + this.allocRateNorm.toString());
        writer.println("- " + this.error.toString());
        writer.flush();
    }

    @Override
    public List<Artifact<?>> asArtifactRow() {
        return List.of(this.allocRateNorm, this.error);
    }
}
