package comparator.warmup.jmh;

import java.util.Objects;

public final class JMHScore {
    private final String benchmark;
    private final double score;
    private final String unit;

    public JMHScore(final String benchmark, final double score, final String unit) {
        this.benchmark = Objects.requireNonNull(benchmark);
        this.score = score;
        this.unit = Objects.requireNonNull(unit);
    }

    public String benchmark() {
        return this.benchmark;
    }

    public double score() {
        return this.score;
    }

    public String unit() {
        return this.unit;
    }

    @Override
    public String toString() {
        return this.benchmark + ": " + this.score + " " + this.unit;
    }
}
