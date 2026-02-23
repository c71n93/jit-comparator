package comparator.jmh;

import comparator.Artifact;

/**
 * Shared base for JMH metric value objects, providing printing and value
 * access.
 */
public abstract class JMHMetric implements Artifact<Double> {
    private final String name;
    private final double score;
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
    public Double value() {
        return this.score;
    }

    @Override
    public String headerCsv() {
        return this.name + ", " + this.unit;
    }

    @Override
    public String toString() {
        return this.name + ": " + this.score + " " + this.unit;
    }
}
