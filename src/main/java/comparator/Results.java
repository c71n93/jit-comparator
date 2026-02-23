package comparator;

import java.io.OutputStream;
import java.util.List;

/**
 * A collection of JIT artifacts.
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

    /**
     * Prints a human-readable representation of these results.
     *
     * @param out
     *            output stream
     */
    void print(final OutputStream out);
}
