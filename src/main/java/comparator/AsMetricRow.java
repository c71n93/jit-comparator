package comparator;

import java.util.List;

/**
 * A metric-row projection as a vector.
 *
 * <p>
 * This projection is narrower than {@link AsArtifactRow}: it contains only
 * comparable metrics and intentionally excludes more general artifacts such as
 * metric errors.
 * </p>
 */
public interface AsMetricRow {
    /**
     * Returns a row (a vector) of comparable metrics.
     *
     * @return a row of metrics
     */
    List<Metric<?>> asMetricRow();
}
