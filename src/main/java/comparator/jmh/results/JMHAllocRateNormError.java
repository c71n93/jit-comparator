package comparator.jmh.results;

/**
 * Relative error artifact for the normalized allocation-rate metric.
 */
public final class JMHAllocRateNormError extends JMHMetricError {
    private static final String ERROR_NAME = "Allocations relative error";

    /**
     * Ctor.
     *
     * @param relativeError
     *            unitless relative error ratio
     */
    public JMHAllocRateNormError(final double relativeError) {
        super(JMHAllocRateNormError.ERROR_NAME, relativeError);
    }

    /**
     * Ctor.
     *
     * @param score
     *            allocation metric score
     * @param scoreError
     *            absolute JMH score error
     */
    public JMHAllocRateNormError(final double score, final double scoreError) {
        this(JMHMetricError.relativeError(score, scoreError));
    }
}
