package comparator.comparison;

import comparator.JITResults;
import comparator.Metric;
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
     * Metric-level equivalence check for both compared result sets.
     *
     * @return {@code true} when all corresponding metrics are equivalent
     * @throws IllegalArgumentException
     *             if metric rows have different sizes
     */
    public boolean areSame() {
        final List<Metric<?>> leftMetrics = this.left.asMetricRow();
        final List<Metric<?>> rightMetrics = this.right.asMetricRow();
        checkSizeCompatibility(leftMetrics, rightMetrics);
        for (int index = 0; index < leftMetrics.size(); index += 1) {
            if (!leftMetrics.get(index).isSame(rightMetrics.get(index))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Root mean square aggregate of per-metric relative differences. Metric order
     * follows {@link JITResults#asMetricRow()}.
     *
     * @return aggregated relative difference
     * @throws IllegalArgumentException
     *             if metric rows have different sizes
     */
    public double meanRelativeDifference() {
        final List<Metric<?>> leftMetrics = this.left.asMetricRow();
        final List<Metric<?>> rightMetrics = this.right.asMetricRow();
        checkSizeCompatibility(leftMetrics, rightMetrics);
        if (leftMetrics.isEmpty()) {
            return 0.0d;
        }
        double sumSquares = 0.0;
        for (int index = 0; index < leftMetrics.size(); index += 1) {
            final double relDiff = leftMetrics.get(index).relativeDifference(rightMetrics.get(index));
            sumSquares += relDiff * relDiff;
        }
        return Math.sqrt(sumSquares / leftMetrics.size());
    }

    /**
     * Maximum aggregate of per-metric relative differences. Metric order follows
     * {@link JITResults#asMetricRow()}.
     *
     * @return maximum relative difference
     * @throws IllegalArgumentException
     *             if metric rows have different sizes
     */
    public double maxRelativeDifference() {
        final List<Metric<?>> leftMetrics = this.left.asMetricRow();
        final List<Metric<?>> rightMetrics = this.right.asMetricRow();
        checkSizeCompatibility(leftMetrics, rightMetrics);
        double max = 0.0d;
        for (int index = 0; index < leftMetrics.size(); index += 1) {
            final double relDiff = leftMetrics.get(index).relativeDifference(rightMetrics.get(index));
            if (relDiff > max) {
                max = relDiff;
            }
        }
        return max;
    }

    private static void checkSizeCompatibility(final List<Metric<?>> leftMetrics,
            final List<Metric<?>> rightMetrics) {
        if (leftMetrics.size() != rightMetrics.size()) {
            throw new IllegalArgumentException("Can't compare different sets of JIT results.");
        }
    }
}
