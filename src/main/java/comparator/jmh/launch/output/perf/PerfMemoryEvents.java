package comparator.jmh.launch.output.perf;

import java.io.IOException;
import java.util.List;

public final class PerfMemoryEvents {
    private static final MemoryEvents INTEL = new AvailableMemoryEvents(
            "mem_inst_retired.all_loads",
            "mem_inst_retired.all_stores",
            List.of("mem_inst_retired.all_loads", "cpu_core/mem_inst_retired.all_loads"),
            List.of("mem_inst_retired.all_stores", "cpu_core/mem_inst_retired.all_stores")
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

        List<String> loadMetricNames();

        List<String> storeMetricNames();

        public String eventNames();
    }

    public record AvailableMemoryEvents(
            String loadEventName,
            String storeEventName,
            List<String> loadMetricNames,
            List<String> storeMetricNames) implements MemoryEvents {
        public AvailableMemoryEvents {
            loadMetricNames = List.copyOf(loadMetricNames);
            storeMetricNames = List.copyOf(storeMetricNames);
        }

        public AvailableMemoryEvents(final String loadEventName, final String storeEventName) {
            this(loadEventName, storeEventName, List.of(loadEventName), List.of(storeEventName));
        }

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
        public List<String> loadMetricNames() {
            return List.of();
        }

        @Override
        public List<String> storeMetricNames() {
            return List.of();
        }

        @Override
        public String eventNames() {
            return "";
        }
    }
}
