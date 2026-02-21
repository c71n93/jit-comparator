package comparator;

import java.util.List;

/**
 * A textual row projection in CSV column order.
 */
public interface AsCsvRow {
    /**
     * Returns row values in CSV column order.
     *
     * @return a row of string values in CSV column order
     */
    List<String> asCsvRow();
}
