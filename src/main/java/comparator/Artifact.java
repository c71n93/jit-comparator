package comparator;

/**
 * An artifact produced by the JIT compiler for a specific target method. A
 * single artifact corresponds to a single scalar value.
 */
public interface Artifact<T extends Number> {
    double MAX_REL_DIFF = 0.1;
    // TODO: Use some reasonable epsilon here.
    double REL_DIFF_EPSILON = 1.0e-9;

    /**
     * Returns the value of this artifact.
     *
     * @return the value
     */
    T value();

    /**
     * Returns the CSV header for this artifact.
     *
     * @return CSV column header
     */
    String headerCsv();

    /**
     * Determines whether this artifact is considered the same as another.
     *
     * @param other
     *            other artifact to compare
     * @return {@code true} if the artifacts are considered the same
     */
    // TODO: This method is useless now.
    default boolean isSame(final Artifact<?> other) {
        return this.relativeDifference(other) < MAX_REL_DIFF;
    }

    /**
     * Symmetric normalized relative difference between this artifact and another.
     * The value is computed as:
     * {@code 2 * |left - right| / (|left| + |right| + REL_DIFF_EPSILON)}. Equal
     * values produce {@code 0.0}.
     *
     * @param other
     *            artifact to compare
     * @return normalized relative difference value
     */
    default double relativeDifference(final Artifact<?> other) {
        final double left = this.value().doubleValue();
        final double right = other.value().doubleValue();
        final double numerator = 2.0d * Math.abs(left - right);
        final double denominator = Math.abs(left) + Math.abs(right) + Artifact.REL_DIFF_EPSILON;
        return numerator / denominator;
    }
}
