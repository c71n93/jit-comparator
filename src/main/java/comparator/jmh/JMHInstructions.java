package comparator.jmh;

/**
 * Normalized CPU instructions metric reported by the perf profiler.
 */
public final class JMHInstructions extends JMHMetric {
    private static final String METRIC_NAME = "instructions";

    /**
     * Ctor.
     *
     * @param score
     *            numeric value
     * @param unit
     *            measurement unit string
     */
    public JMHInstructions(final double score, final String unit) {
        super(JMHInstructions.METRIC_NAME, score, unit);
    }
}
