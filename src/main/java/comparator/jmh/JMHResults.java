package comparator.jmh;

import comparator.Results;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Stores the results of the forked JMH run: benchmark metrics.
 */
public final class JMHResults implements Results {
    private final JMHPrimaryScore score;
    private final JMHAllocRateNorm allocRateNorm;
    private final JMHPerfResults perf;

    public JMHResults(final JMHPrimaryScore score, final JMHAllocRateNorm allocRateNorm) {
        this(score, allocRateNorm, JMHPerfResults.absent());
    }

    public JMHResults(final JMHPrimaryScore score, final JMHAllocRateNorm allocRateNorm,
            final Optional<JMHInstructions> instructions) {
        this(score, allocRateNorm, instructions, Optional.empty());
    }

    public JMHResults(final JMHPrimaryScore score, final JMHAllocRateNorm allocRateNorm,
            final Optional<JMHInstructions> instructions, final Optional<JMHMemoryLoads> memoryLoads) {
        this(score, allocRateNorm, JMHPerfResults.from(instructions, memoryLoads));
    }

    public JMHResults(final JMHPrimaryScore score, final JMHAllocRateNorm allocRateNorm, final JMHPerfResults perf) {
        this.score = score;
        this.allocRateNorm = allocRateNorm;
        this.perf = perf;
    }

    public JMHPrimaryScore primaryScore() {
        return this.score;
    }

    public JMHAllocRateNorm allocRateNorm() {
        return this.allocRateNorm;
    }

    public Optional<JMHInstructions> instructions() {
        return this.perf.instructions();
    }

    public Optional<JMHMemoryLoads> memoryLoads() {
        return this.perf.memoryLoads();
    }

    @Override
    public void print(final OutputStream out) {
        final PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
        writer.println("JMH results:");
        writer.println("- " + this.score.toString());
        writer.println("- " + this.allocRateNorm.toString());
        this.perf.print(out);
        writer.flush();
    }

    @Override
    public List<String> asRow() {
        final List<String> row = new ArrayList<>(List.of(
                String.valueOf(this.score.value()),
                String.valueOf(this.allocRateNorm.value())
        ));
        row.addAll(this.perf.asRow());
        return row;
    }
}
