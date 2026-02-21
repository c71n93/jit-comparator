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
    public List<String> asCsvRow() {
        final List<String> row = new ArrayList<>();
        row.addAll(this.jmh.asCsvRow());
        row.addAll(this.jitlog.asCsvRow());
        return row;
    }

    @Override
    public List<Artifact<?>> asArtifactRow() {
        final List<Artifact<?>> row = new ArrayList<>();
        row.addAll(this.jmh.asArtifactRow());
        row.addAll(this.jitlog.asArtifactRow());
        return row;
    }

    /**
     * Compares this result set with another using artifact-level equivalence in a
     * priority order. Now order is defined by asArtifactRow method.
     *
     * @param other
     *            other results to compare
     * @return {@code true} if all tracked artifacts are considered equivalent
     */
    public boolean isSame(final JITResults other) {
        final List<Artifact<?>> thisArtifacts = this.asArtifactRow();
        final List<Artifact<?>> otherArtifacts = other.asArtifactRow();
        checkSizeCompatibility(thisArtifacts, otherArtifacts);
        for (int index = 0; index < thisArtifacts.size(); index += 1) {
            if (!thisArtifacts.get(index).isSame(otherArtifacts.get(index))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Root mean square aggregate of per-artifact relative differences. The artifact
     * order is defined by {@link #asArtifactRow()}.
     *
     * @param other
     *            other results to compare
     * @return aggregated relative difference
     * @throws IllegalArgumentException
     *             if artifact rows have different sizes
     */
    // TODO: We need to somehow ensure (statically if it possible) that we compare
    // same types of artifacts here.
    public double relativeDifference(final JITResults other) {
        final List<Artifact<?>> thisArtifacts = this.asArtifactRow();
        final List<Artifact<?>> otherArtifacts = other.asArtifactRow();
        checkSizeCompatibility(thisArtifacts, otherArtifacts);
        double sumSquares = 0.0;
        for (int index = 0; index < thisArtifacts.size(); index += 1) {
            final double relDiff = thisArtifacts.get(index).relativeDifference(otherArtifacts.get(index));
            sumSquares += relDiff * relDiff;
        }
        return Math.sqrt(sumSquares / thisArtifacts.size());
    }

    private static void checkSizeCompatibility(final List<Artifact<?>> a1, final List<Artifact<?>> a2) {
        if (a1.size() != a2.size()) {
            throw new IllegalArgumentException("Can't compare different sets of JIT results.");
        }
    }
}
