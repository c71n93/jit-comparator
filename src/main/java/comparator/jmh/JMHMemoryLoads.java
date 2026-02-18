package comparator.jmh;

/**
 * Normalized CPU memory loads metric reported by the perf profiler.
 */
public final class JMHMemoryLoads extends JMHMetric {
    private static final String METRIC_NAME = "memory loads";

    /**
     * Ctor.
     *
     * @param score
     *            numeric value
     * @param unit
     *            measurement unit string
     */
    public JMHMemoryLoads(final double score, final String unit) {
        super(JMHMemoryLoads.METRIC_NAME, score, unit);
    }
}
