package comparator.jmh.launch.output.perf;

import java.io.IOException;
import java.util.List;

/** Available perf memory event set. */
@SuppressWarnings("PMD.ProhibitPublicStaticMethods")
public final class PerfMemoryEvents {
    /** Intel. */
    private static final MemoryEvents INTEL = new AvailableMemoryEvents(
        "mem_inst_retired.all_loads",
        "mem_inst_retired.all_stores",
        List.of("mem_inst_retired.all_loads", "cpu_core/mem_inst_retired.all_loads"),
        List.of("mem_inst_retired.all_stores", "cpu_core/mem_inst_retired.all_stores")
    );

    /** Amd. */
    private static final MemoryEvents AMD = new AvailableMemoryEvents(
        "ls_dispatch.ld_dispatch", "ls_dispatch.store_dispatch"
    );

    /** Intel available. */
    private static final boolean INTEL_AVAILABLE = PerfMemoryEvents.eventsAvailable(PerfMemoryEvents.INTEL);

    /** Amd available. */
    private static final boolean AMD_AVAILABLE = PerfMemoryEvents.eventsAvailable(PerfMemoryEvents.AMD);

    /** Available events. */
    private static final MemoryEvents AVAILABLE_EVENTS = PerfMemoryEvents.availableEvents();

    private PerfMemoryEvents() {
    }

    /**
     * events.
     *
     * @return available memory events
     */
    public static MemoryEvents events() {
        return PerfMemoryEvents.AVAILABLE_EVENTS;
    }

    /**
     * memEventsAvailable.
     *
     * @return true when memory events are available
     */
    public static boolean memEventsAvailable() {
        return PerfMemoryEvents.INTEL_AVAILABLE || PerfMemoryEvents.AMD_AVAILABLE;
    }

    /**
     * memEventsAvailableIntel.
     *
     * @return true when Intel memory events are available
     */
    public static boolean memEventsAvailableIntel() {
        return PerfMemoryEvents.INTEL_AVAILABLE;
    }

    /**
     * memEventsAvailableAMD.
     *
     * @return true when AMD memory events are available
     */
    public static boolean memEventsAvailableAMD() {
        return PerfMemoryEvents.AMD_AVAILABLE;
    }

    // @checkstyle ReturnCount (16 lines)
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

    // @checkstyle ReturnCount (10 lines)
    private static MemoryEvents availableEvents() {
        if (PerfMemoryEvents.INTEL_AVAILABLE) {
            return PerfMemoryEvents.INTEL;
        }
        if (PerfMemoryEvents.AMD_AVAILABLE) {
            return PerfMemoryEvents.AMD;
        }
        return new PerfMemoryEvents.EmptyEvents();
    }

    /** MemoryEvents. */
    public sealed interface MemoryEvents permits PerfMemoryEvents.AvailableMemoryEvents, PerfMemoryEvents.EmptyEvents {
        /**
         * loadEventName.
         *
         * @return load event name
         */
        String loadEventName();

        /**
         * storeEventName.
         *
         * @return store event name
         */
        String storeEventName();

        /**
         * loadMetricNames.
         *
         * @return load metric names
         */
        List<String> loadMetricNames();

        /**
         * storeMetricNames.
         *
         * @return store metric names
         */
        List<String> storeMetricNames();

        /**
         * eventNames.
         *
         * @return comma-separated event names
         */
        String eventNames();
    }

    /**
     * AvailableMemoryEvents.
     *
     * @param loadEventName load event name
     * @param storeEventName store event name
     * @param loadMetricNames load metric names
     * @param storeMetricNames store metric names
     */
    public record AvailableMemoryEvents(
                                        String loadEventName,
                                        String storeEventName,
                                        List<String> loadMetricNames,
                                        List<String> storeMetricNames)
        implements MemoryEvents {
        /** Documented member. */
        public AvailableMemoryEvents {
            loadMetricNames = List.copyOf(loadMetricNames);
            storeMetricNames = List.copyOf(storeMetricNames);
        }

        /**
         * AvailableMemoryEvents.
         *
         * @param loadEventName load event name
         * @param storeEventName store event name
         */
        public AvailableMemoryEvents(final String loadEventName, final String storeEventName) {
            this(loadEventName, storeEventName, List.of(loadEventName), List.of(storeEventName));
        }

        @Override
        public String eventNames() {
            return this.loadEventName + "," + this.storeEventName;
        }
    }

    /** EmptyEvents. */
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
