package comparator.jmh.launch;

import comparator.property.Properties;
import comparator.property.PropertyInt;
import comparator.property.PropertyString;
import java.util.List;
import org.openjdk.jmh.runner.options.TimeValue;

/**
 * JMH launch parameters encoded as JVM system properties.
 */
public final class JMHConfig implements Properties {
    // TODO: to find parameters that will be suitable for our task. Noise in the
    // values of jit artifacts should be low.
    private static final int DEFAULT_WARMUP_ITERATIONS = 3;
    private static final TimeValue DEFAULT_WARMUP_TIME = TimeValue.seconds(5);
    private static final int DEFAULT_MEASUREMENT_ITERATIONS = 3;
    private static final TimeValue DEFAULT_MEASUREMENT_TIME = TimeValue.seconds(5);
    private static final PropertyInt WARMUP_ITERATIONS_PROPERTY = new PropertyInt("jmh.warmupIterations");
    private static final PropertyString WARMUP_TIME_PROPERTY = new PropertyString("jmh.warmupTime");
    private static final PropertyInt MEASUREMENT_ITERATIONS_PROPERTY = new PropertyInt("jmh.measurementIterations");
    private static final PropertyString MEASUREMENT_TIME_PROPERTY = new PropertyString("jmh.measurementTime");
    private final int warmupIterations;
    private final TimeValue warmupTime;
    private final int measurementIterations;
    private final TimeValue measurementTime;

    /**
     * Ctor.
     */
    public JMHConfig() {
        this(
                JMHConfig.DEFAULT_WARMUP_ITERATIONS,
                JMHConfig.DEFAULT_WARMUP_TIME,
                JMHConfig.DEFAULT_MEASUREMENT_ITERATIONS,
                JMHConfig.DEFAULT_MEASUREMENT_TIME
        );
    }

    /**
     * Ctor.
     *
     * @param warmupIterations
     *            warmup iterations
     * @param warmupTime
     *            warmup time
     * @param measurementIterations
     *            measurement iterations
     * @param measurementTime
     *            measurement time
     */
    public JMHConfig(final int warmupIterations, final TimeValue warmupTime, final int measurementIterations,
            final TimeValue measurementTime) {
        this.warmupIterations = warmupIterations;
        this.warmupTime = warmupTime;
        this.measurementIterations = measurementIterations;
        this.measurementTime = measurementTime;
    }

    /**
     * @return warmup iterations
     */
    public int warmupIterations() {
        return this.warmupIterations;
    }

    /**
     * @return warmup time
     */
    public TimeValue warmupTime() {
        return this.warmupTime;
    }

    /**
     * @return measurement iterations
     */
    public int measurementIterations() {
        return this.measurementIterations;
    }

    /**
     * @return measurement time
     */
    public TimeValue measurementTime() {
        return this.measurementTime;
    }

    // TODO: find a way to add fromProperties static method to the contract of
    // Properties interface.
    public static JMHConfig fromProperties() {
        return new JMHConfig(
                JMHConfig.WARMUP_ITERATIONS_PROPERTY.requireValue(),
                TimeValue.fromString(JMHConfig.WARMUP_TIME_PROPERTY.requireValue()),
                JMHConfig.MEASUREMENT_ITERATIONS_PROPERTY.requireValue(),
                TimeValue.fromString(JMHConfig.MEASUREMENT_TIME_PROPERTY.requireValue())
        );
    }

    @Override
    public List<String> asJvmArgs() {
        return List.of(
                JMHConfig.WARMUP_ITERATIONS_PROPERTY.asJvmArg(this.warmupIterations),
                JMHConfig.WARMUP_TIME_PROPERTY.asJvmArg(this.asJmhTime(this.warmupTime)),
                JMHConfig.MEASUREMENT_ITERATIONS_PROPERTY.asJvmArg(this.measurementIterations),
                JMHConfig.MEASUREMENT_TIME_PROPERTY.asJvmArg(this.asJmhTime(this.measurementTime))
        );
    }

    private String asJmhTime(final TimeValue value) {
        return value.getTime() + TimeValue.tuToString(value.getTimeUnit());
    }
}
