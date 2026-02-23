package comparator.jmh;

/**
 * Primary JMH metric for the benchmark score.
 */
public final class JMHPrimaryScore extends JMHMetric {
    private static final String METRIC_NAME = "JMH primary score";

    /**
     * Ctor.
     *
     * @param score
     *            numeric value
     * @param unit
     *            measurement unit string
     */
    public JMHPrimaryScore(final double score, final String unit) {
        super(JMHPrimaryScore.METRIC_NAME, score, unit);
    }
}
