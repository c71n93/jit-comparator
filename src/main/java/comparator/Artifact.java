package comparator;

/**
 * A scalar analysis value exported by the toolchain.
 *
 * <p>
 * Artifacts are the most general row items used for CSV and textual output.
 * Comparable benchmark and JIT values are modeled as {@link Metric metrics},
 * while non-comparable values may remain plain artifacts or be modeled as
 * {@link MetricError metric errors} later.
 * </p>
 */
public interface Artifact<T extends Number> {
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
}
