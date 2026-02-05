package comparator;

/**
 * An artifact produced by the JIT compiler for a specific target method. A
 * single artifact corresponds to a single scalar value.
 */
public interface Artifact<T extends Number> {
    double ACCURACY = 0.9;

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
        final double baseVal = this.value().doubleValue();
        final double otherVal = other.value().doubleValue();
        final double diff = Math.abs(baseVal - otherVal);
        if (baseVal == 0.0d) {
            return diff == 0.0d;
        } else {
            final double relDiff = diff / Math.abs(baseVal);
            return relDiff < (1.0d - Artifact.ACCURACY);
        }
    }
}
