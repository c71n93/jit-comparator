package comparator.jmh.results;

/**
 * Normalized CPU memory loads metric reported by the perf profiler.
 */
public final class JMHMemoryLoads extends JMHMetric {
    /** Metric name. */
    private static final String METRIC_NAME = "Memory loads";

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
