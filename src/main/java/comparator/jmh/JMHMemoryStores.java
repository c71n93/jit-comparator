package comparator.jmh;

/**
 * Normalized CPU memory stores metric reported by the perf profiler.
 */
public final class JMHMemoryStores extends JMHMetric {
    static final String DEFAULT_UNIT = "#/op";
    private static final String METRIC_NAME = "Memory stores";

    /**
     * Ctor.
     *
     * @param score
     *            numeric value
     * @param unit
     *            measurement unit string
     */
    public JMHMemoryStores(final double score, final String unit) {
        super(JMHMemoryStores.METRIC_NAME, score, unit);
    }
}
