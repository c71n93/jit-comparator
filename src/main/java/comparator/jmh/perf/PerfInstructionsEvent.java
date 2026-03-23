package comparator.jmh.perf;

import comparator.jmh.JMHInstructions;
import java.util.Optional;

/**
 * Instruction-counter variants emitted by the perf profiler.
 */
public final class PerfInstructionsEvent {
    private static final String EVENT_NAME = "instructions";
    private static final String HYBRID_EVENT_SUFFIX = "/instructions";

    private PerfInstructionsEvent() {
    }

    public static String eventName() {
        return PerfInstructionsEvent.EVENT_NAME;
    }

    public static Optional<JMHInstructions> metric(final PerfSecondaryMetrics metrics) {
        final Optional<PerfMetric> direct = metrics.metric(PerfInstructionsEvent.EVENT_NAME);
        if (direct.isPresent()) {
            return direct.map(PerfInstructionsEvent::instructions);
        }
        return metrics.summedMetric(baseName -> baseName.endsWith(PerfInstructionsEvent.HYBRID_EVENT_SUFFIX))
                .map(PerfInstructionsEvent::instructions);
    }

    private static JMHInstructions instructions(final PerfMetric metric) {
        return new JMHInstructions(metric.score(), metric.unit());
    }
}
