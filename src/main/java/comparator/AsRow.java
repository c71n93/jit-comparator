package comparator;

/**
 * A composite row contract with textual, artifact, and metric projections of
 * the same data.
 *
 * @see AsCsvRow
 * @see AsArtifactRow
 * @see AsMetricRow
 */
public interface AsRow extends AsCsvRow, AsArtifactRow, AsMetricRow {
}
