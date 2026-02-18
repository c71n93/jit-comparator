package comparator.jmh;

import comparator.Results;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Stores the results of the forked JMH run: benchmark metrics.
 */
public final class JMHResults implements Results {
    private final JMHPrimaryScore score;
    private final JMHAllocRateNorm allocRateNorm;
    // TODO: Combine these artifacts into JMHPerfResults.
    private final Optional<JMHInstructions> instructions;
    private final Optional<JMHMemoryLoads> memoryLoads;

    public JMHResults(final JMHPrimaryScore score, final JMHAllocRateNorm allocRateNorm) {
        this(score, allocRateNorm, Optional.empty(), Optional.empty());
    }

    public JMHResults(final JMHPrimaryScore score, final JMHAllocRateNorm allocRateNorm,
            final Optional<JMHInstructions> instructions) {
        this(score, allocRateNorm, instructions, Optional.empty());
    }

    public JMHResults(final JMHPrimaryScore score, final JMHAllocRateNorm allocRateNorm,
            final Optional<JMHInstructions> instructions, final Optional<JMHMemoryLoads> memoryLoads) {
        this.score = Objects.requireNonNull(score);
        this.allocRateNorm = Objects.requireNonNull(allocRateNorm);
        this.instructions = Objects.requireNonNull(instructions);
        this.memoryLoads = Objects.requireNonNull(memoryLoads);
    }

    public JMHPrimaryScore primaryScore() {
        return this.score;
    }

    public JMHAllocRateNorm allocRateNorm() {
        return this.allocRateNorm;
    }

    public Optional<JMHInstructions> instructions() {
        return this.instructions;
    }

    public Optional<JMHMemoryLoads> memoryLoads() {
        return this.memoryLoads;
    }

    @Override
    public void print(final OutputStream out) {
        final PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
        writer.println("JMH results:");
        writer.println("- " + this.score.toString());
        writer.println("- " + this.allocRateNorm.toString());
        this.instructions.ifPresent(metric -> writer.println("- " + metric.toString()));
        this.memoryLoads.ifPresent(metric -> writer.println("- " + metric.toString()));
        writer.flush();
    }

    @Override
    public List<String> asRow() {
        return List.of(
                String.valueOf(this.score.value()),
                String.valueOf(this.allocRateNorm.value()),
                this.instructions.map(metric -> String.valueOf(metric.value())).orElse(""),
                this.memoryLoads.map(metric -> String.valueOf(metric.value())).orElse("")
        );
    }
}
