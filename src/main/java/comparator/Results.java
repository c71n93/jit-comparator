package comparator;

import java.io.OutputStream;
import java.util.List;

/**
 * A collection of JIT artifacts.
 */
public interface Results extends AsRow {
    /**
     * Prints a human-readable representation of these results.
     *
     * @param out
     *            output stream
     */
    void print(final OutputStream out);

    @Override
    default List<Artifact<?>> asArtifactRow() {
        // TODO Temporary to avoid compilation errors
        throw new UnsupportedOperationException("Unimplemented method 'asArtifactRow'");
    }
}
