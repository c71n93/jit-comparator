package comparator;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A collection of analysis artifacts.
 *
 * <p>
 * Results expose the full artifact row for reporting and a filtered metric row
 * for comparison logic.
 * </p>
 */
public interface Results extends AsRow {
    @Override
    default List<String> asCsvRow() {
        return this.asArtifactRow().stream().map(artifact -> String.valueOf(artifact.value())).toList();
    }

    @Override
    default List<String> headerCsv() {
        return this.asArtifactRow().stream().map(Artifact::headerCsv).toList();
    }

    @Override
    default List<Metric<?>> asMetricRow() {
        final List<Metric<?>> row = new ArrayList<>();
        for (final Artifact<?> artifact : this.asArtifactRow()) {
            if (artifact instanceof Metric<?> metric) {
                row.add(metric);
            }
        }
        return row;
    }

    /**
     * Prints a human-readable representation of these results.
     *
     * @param out
     *            output stream
     */
    void print(final OutputStream out);
}
