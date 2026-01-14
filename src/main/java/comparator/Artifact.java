package comparator;

/**
 * An artifact produced by the JIT compiler for a specific target method. A
 * single artifact corresponds to a single scalar value.
 */
public interface Artifact<T extends Number> {
    /**
     * Returns the value of this artifact.
     *
     * @return the value
     */
    T value();
}
