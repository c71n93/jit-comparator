package comparator;

import java.io.OutputStream;

/**
 * A collection of different JIT artifacts.
 */
public interface Results extends AsRow {
    void print(final OutputStream out);
}
