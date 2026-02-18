package comparator.jmh.launch;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.runner.options.TimeValue;

class JMHConfigTest {
    @Test
    void rendersJvmArgs() {
        final JMHConfig config = new JMHConfig(2, TimeValue.milliseconds(150), 3, TimeValue.seconds(2), true);
        Assertions.assertEquals(
                List.of(
                        "-Djmh.warmupIterations=2",
                        "-Djmh.warmupTime=150ms",
                        "-Djmh.measurementIterations=3",
                        "-Djmh.measurementTime=2s",
                        "-Djmh.perf.enabled=true"
                ),
                config.asJvmPropertyArgs(),
                "JMH config should render JVM properties"
        );
    }

    @Test
    void loadsFromProperties() {
        final String warmupIterationsKey = "jmh.warmupIterations";
        final String warmupTimeKey = "jmh.warmupTime";
        final String measurementIterationsKey = "jmh.measurementIterations";
        final String measurementTimeKey = "jmh.measurementTime";
        final String perfEnabledKey = "jmh.perf.enabled";
        final String previousWarmupIterations = System.getProperty(warmupIterationsKey);
        final String previousWarmupTime = System.getProperty(warmupTimeKey);
        final String previousMeasurementIterations = System.getProperty(measurementIterationsKey);
        final String previousMeasurementTime = System.getProperty(measurementTimeKey);
        final String previousPerfEnabled = System.getProperty(perfEnabledKey);
        System.setProperty(warmupIterationsKey, "4");
        System.setProperty(warmupTimeKey, "70ms");
        System.setProperty(measurementIterationsKey, "5");
        System.setProperty(measurementTimeKey, "3s");
        System.setProperty(perfEnabledKey, "true");
        try {
            final JMHConfig config = JMHConfig.fromProperties();
            Assertions.assertEquals(4, config.warmupIterations(), "Warmup iterations should be loaded");
            Assertions.assertEquals(TimeValue.milliseconds(70), config.warmupTime(), "Warmup time should be loaded");
            Assertions.assertEquals(5, config.measurementIterations(), "Measurement iterations should be loaded");
            Assertions
                    .assertEquals(TimeValue.seconds(3), config.measurementTime(), "Measurement time should be loaded");
            Assertions.assertTrue(config.perfEnabled(), "Perf flag should be loaded");
        } finally {
            this.restoreProperty(warmupIterationsKey, previousWarmupIterations);
            this.restoreProperty(warmupTimeKey, previousWarmupTime);
            this.restoreProperty(measurementIterationsKey, previousMeasurementIterations);
            this.restoreProperty(measurementTimeKey, previousMeasurementTime);
            this.restoreProperty(perfEnabledKey, previousPerfEnabled);
        }
    }

    private void restoreProperty(final String key, final String value) {
        if (value == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, value);
        }
    }
}
