package comparator;

import java.util.List;

/**
 * An artifact row projection as a vector.
 *
 * @see AsCsvRow
 */
public interface AsArtifactRow {
    /**
     * Returns a row (a vector) of artifacts.
     *
     * @return a row of artifacts
     */
    List<Artifact<?>> asArtifactRow();
}
