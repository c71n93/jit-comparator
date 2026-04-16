package comparator.jmh.results;

import comparator.Metric;

/**
 * Shared base for JMH metric value objects, providing printing and value
 * access.
 */
public abstract class JMHMetric implements Metric<Double> {
    /** Property name. */
    private final String name;

    /** Metric score. */
    private final double score;

    /** Measurement unit. */
    private final String unit;

    /**
     * Ctor.
     *
     * @param name
     *            metric identifier from the JMH JSON output
     * @param score
     *            numeric value
     * @param unit
     *            measurement unit string
     */
    protected JMHMetric(final String name, final double score, final String unit) {
        this.name = name;
        this.score = score;
        this.unit = unit;
    }

    @Override
    public final Double value() {
        return this.score;
    }

    @Override
    public final String headerCsv() {
        return this.name + ", " + this.unit;
    }

    @Override
    public final String toString() {
        return this.name + ": " + this.score + " " + this.unit;
    }
}
