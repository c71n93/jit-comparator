package comparator.comparison;

import comparator.Artifact;
import comparator.JITResults;
import java.util.List;

/**
 * Pairwise comparison of two {@link JITResults} objects.
 */
public final class JITResultsComparison {
    private final JITResults left;
    private final JITResults right;

    /**
     * Ctor.
     *
     * @param left
     *            left-side results
     * @param right
     *            right-side results
     */
    public JITResultsComparison(final JITResults left, final JITResults right) {
        this.left = left;
        this.right = right;
    }

    /**
     * Artifact-level equivalence check for both compared result sets.
     *
     * @return {@code true} when all corresponding artifacts are equivalent
     * @throws IllegalArgumentException
     *             if artifact rows have different sizes
     */
    public boolean areSame() {
        final List<Artifact<?>> leftArtifacts = this.left.asArtifactRow();
        final List<Artifact<?>> rightArtifacts = this.right.asArtifactRow();
        checkSizeCompatibility(leftArtifacts, rightArtifacts);
        for (int index = 0; index < leftArtifacts.size(); index += 1) {
            if (!leftArtifacts.get(index).isSame(rightArtifacts.get(index))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Root mean square aggregate of per-artifact relative differences. Artifact
     * order follows {@link JITResults#asArtifactRow()}.
     *
     * @return aggregated relative difference
     * @throws IllegalArgumentException
     *             if artifact rows have different sizes
     */
    public double meanRelativeDifference() {
        final List<Artifact<?>> leftArtifacts = this.left.asArtifactRow();
        final List<Artifact<?>> rightArtifacts = this.right.asArtifactRow();
        checkSizeCompatibility(leftArtifacts, rightArtifacts);
        if (leftArtifacts.isEmpty()) {
            return 0.0d;
        }
        double sumSquares = 0.0;
        for (int index = 0; index < leftArtifacts.size(); index += 1) {
            final double relDiff = leftArtifacts.get(index).relativeDifference(rightArtifacts.get(index));
            sumSquares += relDiff * relDiff;
        }
        return Math.sqrt(sumSquares / leftArtifacts.size());
    }

    /**
     * Maximum aggregate of per-artifact relative differences. Artifact order
     * follows {@link JITResults#asArtifactRow()}.
     *
     * @return maximum relative difference
     * @throws IllegalArgumentException
     *             if artifact rows have different sizes
     */
    // TODO: We can combine maxRelativeDifference and meanRelativeDifference in a
    // single method, because they are making same computations twice.
    public double maxRelativeDifference() {
        final List<Artifact<?>> leftArtifacts = this.left.asArtifactRow();
        final List<Artifact<?>> rightArtifacts = this.right.asArtifactRow();
        checkSizeCompatibility(leftArtifacts, rightArtifacts);
        double max = 0.0d;
        for (int index = 0; index < leftArtifacts.size(); index += 1) {
            final double relDiff = leftArtifacts.get(index).relativeDifference(rightArtifacts.get(index));
            if (relDiff > max) {
                max = relDiff;
            }
        }
        return max;
    }

    private static void checkSizeCompatibility(final List<Artifact<?>> leftArtifacts,
            final List<Artifact<?>> rightArtifacts) {
        if (leftArtifacts.size() != rightArtifacts.size()) {
            throw new IllegalArgumentException("Can't compare different sets of JIT results.");
        }
    }
}
