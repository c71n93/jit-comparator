package comparator.jmh.metrics;

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
public sealed interface JMHPerfResults extends Results permits JMHPerfResults.Present, JMHPerfResults.Absent {
    static JMHPerfResults absent() {
        return Absent.INSTANCE;
    }

    static JMHPerfResults from(final JMHInstructions instructions, final JMHMemoryLoads memoryLoads,
            final JMHMemoryStores memoryStores) {
        return new Present(instructions, memoryLoads, memoryStores);
    }

    static JMHPerfResults from(final JMHInstructions instructions) {
        return new Present(instructions);
    }

    @Override
    List<Artifact<?>> asArtifactRow();

    @Override
    void print(final OutputStream out);

    /**
     * Present perf-profiler metrics.
     */
    final class Present implements JMHPerfResults {
        private final JMHInstructions instructions;
        private final Optional<JMHMemoryLoads> memoryLoads;
        private final Optional<JMHMemoryStores> memoryStores;

        Present(final JMHInstructions instructions) {
            this.instructions = instructions;
            this.memoryLoads = Optional.empty();
            this.memoryStores = Optional.empty();
        }

        Present(final JMHInstructions instructions, final JMHMemoryLoads memoryLoads,
                final JMHMemoryStores memoryStores) {
            this.instructions = instructions;
            this.memoryLoads = Optional.of(memoryLoads);
            this.memoryStores = Optional.of(memoryStores);
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
        private static final Absent INSTANCE = new Absent();

        private Absent() {
        }

        @Override
        public List<Artifact<?>> asArtifactRow() {
            return List.of();
        }

        @Override
        public void print(final OutputStream out) {
        }
    }
}
