package comparator.jmh.results;

import comparator.MetricError;

/**
 * Shared base for JMH metric-relative-error artifacts.
 */
abstract class JMHMetricError implements MetricError {
    private final String name;
    private final double value;

    /**
     * Ctor.
     *
     * @param name
     *            error artifact name
     * @param value
     *            relative error ratio
     */
    protected JMHMetricError(final String name, final double value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Relative-error ratio derived from the absolute JMH score error.
     *
     * @param score
     *            metric score
     * @param scoreError
     *            absolute JMH score error
     * @return unitless relative error ratio
     */
    protected static double relativeError(final double score, final double scoreError) {
        if (Double.compare(score, 0.0d) == 0) {
            return Double.NaN;
        }
        return Math.abs(scoreError) / Math.abs(score);
    }

    @Override
    public final Double value() {
        return this.value;
    }

    @Override
    public final String headerCsv() {
        return this.name + ", ratio";
    }

    @Override
    public final String toString() {
        return this.name + ": " + this.value + " ratio";
    }
}
