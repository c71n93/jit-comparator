package comparator.jmh.results;

/**
 * Relative error artifact for the primary JMH score.
 */
public final class JMHPrimaryScoreError extends JMHMetricError {
    private static final String ERROR_NAME = "JMH primary score relative error";

    /**
     * Ctor.
     *
     * @param relativeError
     *            unitless relative error ratio
     */
    public JMHPrimaryScoreError(final double relativeError) {
        super(JMHPrimaryScoreError.ERROR_NAME, relativeError);
    }

    /**
     * Ctor.
     *
     * @param score
     *            primary score
     * @param scoreError
     *            absolute JMH score error
     */
    public JMHPrimaryScoreError(final double score, final double scoreError) {
        this(JMHMetricError.relativeError(score, scoreError));
    }
}
