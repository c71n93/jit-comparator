package comparator.jmh.results;

import comparator.Artifact;
import comparator.Results;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/**
 * Aggregated optional perf-profiler metrics.
 */
@SuppressWarnings("PMD.ProhibitPublicStaticMethods")
public sealed interface JMHPerfResults extends Results permits JMHPerfResults.Present, JMHPerfResults.Absent {
    /**
     * absent.
     *
     * @return absent perf results
     */
    static JMHPerfResults absent() {
        return Absent.INSTANCE;
    }

    /**
     * from.
     *
     * @param instructions instruction metric
     * @param memoryLoads memory load metric
     * @param memoryStores memory store metric
     * @return present perf results
     */
    static JMHPerfResults from(final JMHInstructions instructions, final JMHMemoryLoads memoryLoads,
                               final JMHMemoryStores memoryStores) {
        return new Present(instructions, memoryLoads, memoryStores);
    }

    /**
     * from.
     *
     * @param instructions instruction metric
     * @return present perf results
     */
    static JMHPerfResults from(final JMHInstructions instructions) {
        return new Present(instructions);
    }

    @Override
    List<Artifact<?>> asArtifactRow();

    @Override
    void print(OutputStream out);

    /**
     * Present perf-profiler metrics.
     */
    final class Present implements JMHPerfResults {
        /** Instructions. */
        private final JMHInstructions instructions;

        /** Memory loads. */
        private final Optional<JMHMemoryLoads> memoryLoads;

        /** Memory stores. */
        private final Optional<JMHMemoryStores> memoryStores;

        /**
         * Present.
         *
         * @param instructions instruction metric
         */
        private Present(final JMHInstructions instructions) {
            this(instructions, Optional.empty(), Optional.empty());
        }

        /**
         * Present.
         *
         * @param instructions instruction metric
         * @param memoryLoads memory load metric
         * @param memoryStores memory store metric
         */
        private Present(final JMHInstructions instructions, final JMHMemoryLoads memoryLoads,
                        final JMHMemoryStores memoryStores) {
            this(instructions, Optional.of(memoryLoads), Optional.of(memoryStores));
        }

        private Present(final JMHInstructions instructions, final Optional<JMHMemoryLoads> memoryLoads,
                        final Optional<JMHMemoryStores> memoryStores) {
            this.instructions = instructions;
            this.memoryLoads = memoryLoads;
            this.memoryStores = memoryStores;
        }

        @Override
        public List<Artifact<?>> asArtifactRow() {
            if (this.memoryLoads.isPresent() && this.memoryStores.isPresent()) {
                return List.of(this.instructions, this.memoryLoads.orElseThrow(), this.memoryStores.orElseThrow());
            }
            return List.of(this.instructions);
        }

        @Override
        public void print(final OutputStream out) {
            final PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
            writer.println("- " + this.instructions.toString());
            this.memoryLoads.ifPresent(metric -> writer.println("- " + metric.toString()));
            this.memoryStores.ifPresent(metric -> writer.println("- " + metric.toString()));
            writer.flush();
        }
    }

    /**
     * Missing perf-profiler metrics.
     */
    final class Absent implements JMHPerfResults {
        /** Instance. */
        private static final Absent INSTANCE = new Absent();

        private Absent() {
        }

        @Override
        public List<Artifact<?>> asArtifactRow() {
            return List.of();
        }

        @Override
        public void print(final OutputStream out) {
            // Intentionally empty.
        }
    }
}
