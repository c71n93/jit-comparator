package comparator.warmup.jmh;

import comparator.Artifact;

public class JMHAllocRateNorm implements Artifact {
    private static final String METRIC_NAME = "gc.alloc.rate.norm";
    private final double score;
    private final String unit;

    public JMHAllocRateNorm(final double score, final String unit) {
        this.score = score;
        this.unit = unit;
    }

    public double score() {
        return this.score;
    }

    public String unit() {
        return this.unit;
    }

    @Override
    public String toString() {
        return JMHAllocRateNorm.METRIC_NAME + ": " + this.score + " " + this.unit;
    }
}
