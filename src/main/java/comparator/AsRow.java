package comparator;

/**
 * A composite row contract with textual and artifact projections of the same
 * data.
 *
 * @see AsCsvRow
 * @see AsArtifactRow
 */
public interface AsRow extends AsCsvRow, AsArtifactRow {
}
