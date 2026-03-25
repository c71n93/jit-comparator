package comparator.jmh.launch.output.perf;

import java.io.IOException;

public final class PerfMemoryEvents {
    private static final MemoryEvents INTEL = new AvailableMemoryEvents(
            "cpu_core/mem_inst_retired.all_loads/", "cpu_core/mem_inst_retired.all_stores/"
    );
    private static final MemoryEvents AMD = new AvailableMemoryEvents(
            "ls_dispatch.ld_dispatch", "ls_dispatch.store_dispatch"
    );
    private static final boolean INTEL_AVAILABLE = PerfMemoryEvents.eventsAvailable(PerfMemoryEvents.INTEL);
    private static final boolean AMD_AVAILABLE = PerfMemoryEvents.eventsAvailable(PerfMemoryEvents.AMD);
    private static final MemoryEvents AVAILABLE_EVENTS = PerfMemoryEvents.availableEvents();

    private PerfMemoryEvents() {
    }

    public static MemoryEvents events() {
        return PerfMemoryEvents.AVAILABLE_EVENTS;
    }

    public static boolean memEventsAvailable() {
        return PerfMemoryEvents.INTEL_AVAILABLE || PerfMemoryEvents.AMD_AVAILABLE;
    }

    public static boolean memEventsAvailableIntel() {
        return PerfMemoryEvents.INTEL_AVAILABLE;
    }

    public static boolean memEventsAvailableAMD() {
        return PerfMemoryEvents.AMD_AVAILABLE;
    }

    private static boolean eventsAvailable(final MemoryEvents events) {
        try {
            final Process process = new ProcessBuilder(
                    "perf", "stat", "-e", events.eventNames(), "echo", "1"
            )
                    .redirectErrorStream(true)
                    .start();
            process.getInputStream().readAllBytes();
            return process.waitFor() == 0;
        } catch (final InterruptedException exception) {
            Thread.currentThread().interrupt();
            return false;
        } catch (final IOException exception) {
            return false;
        }
    }

    private static MemoryEvents availableEvents() {
        if (PerfMemoryEvents.INTEL_AVAILABLE) {
            return PerfMemoryEvents.INTEL;
        }
        if (PerfMemoryEvents.AMD_AVAILABLE) {
            return PerfMemoryEvents.AMD;
        }
        return new PerfMemoryEvents.EmptyEvents();
    }

    public sealed interface MemoryEvents permits PerfMemoryEvents.AvailableMemoryEvents, PerfMemoryEvents.EmptyEvents {
        String loadEventName();

        String storeEventName();

        public String eventNames();

        default String loadMetricName() {
            return this.loadEventName() + ":u";
        }

        default String storeMetricName() {
            return this.storeEventName() + ":u";
        }
    }

    public record AvailableMemoryEvents(String loadEventName, String storeEventName) implements MemoryEvents {
        @Override
        public String eventNames() {
            return this.loadEventName + "," + this.storeEventName;
        }
    }

    public record EmptyEvents() implements MemoryEvents {
        @Override
        public String loadEventName() {
            return "";
        }

        @Override
        public String storeEventName() {
            return "";
        }

        @Override
        public String eventNames() {
            return "";
        }

        @Override
        public String loadMetricName() {
            throw new IllegalStateException("Load metric name is unavailable because memory events are unavailable.");
        }

        @Override
        public String storeMetricName() {
            throw new IllegalStateException("Store metric name is unavailable because memory events are unavailable.");
        }
    }
}
