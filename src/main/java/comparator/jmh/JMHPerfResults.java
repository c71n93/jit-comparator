package comparator.jmh;

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
            final Optional<JMHMemoryLoads> memoryLoads) {
        if (instructions.isPresent() && memoryLoads.isPresent()) {
            return new Present(instructions.orElseThrow(), memoryLoads.orElseThrow());
        }
        return Absent.INSTANCE;
    }

    Optional<JMHInstructions> instructions();

    Optional<JMHMemoryLoads> memoryLoads();

    @Override
    List<String> asRow();

    @Override
    void print(final OutputStream out);

    /**
     * Present perf-profiler metrics.
     */
    final class Present implements JMHPerfResults {
        private final JMHInstructions instructions;
        private final JMHMemoryLoads memoryLoads;

        Present(final JMHInstructions instructions, final JMHMemoryLoads memoryLoads) {
            this.instructions = instructions;
            this.memoryLoads = memoryLoads;
        }

        @Override
        public Optional<JMHInstructions> instructions() {
            return Optional.of(this.instructions);
        }

        @Override
        public Optional<JMHMemoryLoads> memoryLoads() {
            return Optional.of(this.memoryLoads);
        }

        @Override
        public List<String> asRow() {
            return List.of(String.valueOf(this.instructions.value()), String.valueOf(this.memoryLoads.value()));
        }

        @Override
        public void print(final OutputStream out) {
            final PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
            writer.println("- " + this.instructions.toString());
            writer.println("- " + this.memoryLoads.toString());
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
        public Optional<JMHInstructions> instructions() {
            return Optional.empty();
        }

        @Override
        public Optional<JMHMemoryLoads> memoryLoads() {
            return Optional.empty();
        }

        @Override
        public List<String> asRow() {
            return List.of("", "");
        }

        @Override
        public void print(final OutputStream out) {
        }
    }
}
