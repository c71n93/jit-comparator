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
     * Determines whether this artifact is considered the same as another.
     *
     * @param other
     *            other artifact to compare
     * @return {@code true} if the artifacts are considered the same
     */
    // TODO: Temporary implementation. Implement this method for each Artifact when
    // actual accuracy will be chosen.
    default boolean isSame(Artifact<T> other) {
        return this.relativeDifference(other) < MAX_REL_DIFF;
    }

    default double relativeDifference(final Artifact<T> other) {
        final double left = this.value().doubleValue();
        final double right = other.value().doubleValue();
        final double numerator = 2.0d * Math.abs(left - right);
        final double denominator = Math.abs(left) + Math.abs(right) + Artifact.REL_DIFF_EPSILON;
        return numerator / denominator;
    }
}
