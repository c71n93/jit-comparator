package comparator.jmh;

import comparator.Artifact;

public class JMHAllocRateNorm implements Artifact<Double> {
    private static final String METRIC_NAME = "gc.alloc.rate.norm";
    private final double score;
    private final String unit;

    public JMHAllocRateNorm(final double score, final String unit) {
        this.score = score;
        this.unit = unit;
    }

    @Override
    public Double value() {
        return this.score;
    }

    @Override
    public String toString() {
        return JMHAllocRateNorm.METRIC_NAME + ": " + this.score + " " + this.unit;
    }
}
