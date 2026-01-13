package comparator.jmh;

import comparator.Artifact;

// TODO: Reuse code from this class using decoration. Now JMHAllocRateNorm is simply a copy paste of JMHPrimaryScore.
public final class JMHPrimaryScore implements Artifact {
    private static final String METRIC_NAME = "primary";
    private final double score;
    private final String unit;

    public JMHPrimaryScore(final double score, final String unit) {
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
        return JMHPrimaryScore.METRIC_NAME + ": " + this.score + " " + this.unit;
    }
}
