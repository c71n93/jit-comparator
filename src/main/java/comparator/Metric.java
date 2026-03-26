package comparator;

/**
 * A scalar artifact that participates in result comparison.
 *
 * <p>
 * Metrics keep the comparison-specific operations that previously lived on
 * {@link Artifact}. This allows the codebase to retain broader artifact rows
 * for reporting while using only metrics in comparison logic.
 * </p>
 *
 * @param <T>
 *            metric value type
 */
public interface Metric<T extends Number> extends Artifact<T> {
    /**
     * Default upper bound for considering two metrics equivalent.
     */
    double MAX_REL_DIFF = 0.1;

    /**
     * Small constant used to keep relative-difference calculations stable near
     * zero.
     */
    double REL_DIFF_EPSILON = 1.0e-9;

    /**
     * Determines whether this metric is considered the same as another.
     *
     * @param other
     *            other metric to compare
     * @return {@code true} if the metrics are considered the same
     */
    default boolean isSame(final Metric<?> other) {
        return this.relativeDifference(other) < Metric.MAX_REL_DIFF;
    }

    /**
     * Symmetric normalized relative difference between this metric and another. The
     * value is computed as:
     * {@code 2 * |left - right| / (|left| + |right| + REL_DIFF_EPSILON)}. Equal
     * values produce {@code 0.0}.
     *
     * @param other
     *            metric to compare
     * @return normalized relative difference value
     */
    default double relativeDifference(final Metric<?> other) {
        final double left = this.value().doubleValue();
        final double right = other.value().doubleValue();
        final double numerator = 2.0d * Math.abs(left - right);
        final double denominator = Math.abs(left) + Math.abs(right) + Metric.REL_DIFF_EPSILON;
        return numerator / denominator;
    }
}
