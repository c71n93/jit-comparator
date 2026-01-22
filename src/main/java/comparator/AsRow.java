package comparator;

import java.util.List;

/**
 * A row of string values representing a CSV record.
 */
public interface AsRow {
    /**
     * Returns row values in column order.
     *
     * @return row values
     */
    List<String> asRow();
}
