package comparator.jmh;

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

    static JMHPerfResults from(final Optional<JMHInstructions> instructions,
            final Optional<JMHMemoryLoads> memoryLoads, final Optional<JMHMemoryStores> memoryStores) {
        if (instructions.isPresent() && memoryLoads.isPresent() && memoryStores.isPresent()) {
            return new Present(instructions.orElseThrow(), memoryLoads.orElseThrow(), memoryStores.orElseThrow());
        }
        return Absent.INSTANCE;
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
        private final JMHMemoryLoads memoryLoads;
        private final JMHMemoryStores memoryStores;

        Present(final JMHInstructions instructions, final JMHMemoryLoads memoryLoads,
                final JMHMemoryStores memoryStores) {
            this.instructions = instructions;
            this.memoryLoads = memoryLoads;
            this.memoryStores = memoryStores;
        }

        @Override
        public List<Artifact<?>> asArtifactRow() {
            return List.of(this.instructions, this.memoryLoads, this.memoryStores);
        }

        @Override
        public void print(final OutputStream out) {
            final PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
            writer.println("- " + this.instructions.toString());
            writer.println("- " + this.memoryLoads.toString());
            writer.println("- " + this.memoryStores.toString());
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
