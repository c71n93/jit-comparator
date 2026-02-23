package comparator.jmh;

/**
 * Normalized allocation rate metric reported by the GC profiler.
 */
public final class JMHAllocRateNorm extends JMHMetric {
    private static final String METRIC_NAME = "Allocations";

    /**
     * Ctor.
     *
     * @param score
     *            numeric value
     * @param unit
     *            measurement unit string
     */
    public JMHAllocRateNorm(final double score, final String unit) {
        super(JMHAllocRateNorm.METRIC_NAME, score, unit);
    }
}
