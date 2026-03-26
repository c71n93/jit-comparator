package comparator.jmh.results;

import comparator.Artifact;
import comparator.Results;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores the results of the forked JMH run: benchmark metrics.
 */
public final class JMHResults implements Results {
    private final JMHPrimaryScoreResults score;
    private final JMHAllocRateNormResults allocRateNorm;
    private final JMHPerfResults perf;

    public JMHResults(final JMHPrimaryScoreResults score, final JMHAllocRateNormResults allocRateNorm) {
        this(score, allocRateNorm, JMHPerfResults.absent());
    }

    public JMHResults(final JMHPrimaryScoreResults score, final JMHAllocRateNormResults allocRateNorm,
            final JMHInstructions instructions) {
        this(score, allocRateNorm, JMHPerfResults.from(instructions));
    }

    public JMHResults(final JMHPrimaryScoreResults score, final JMHAllocRateNormResults allocRateNorm,
            final JMHInstructions instructions, final JMHMemoryLoads memoryLoads,
            final JMHMemoryStores memoryStores) {
        this(score, allocRateNorm, JMHPerfResults.from(instructions, memoryLoads, memoryStores));
    }

    public JMHResults(final JMHPrimaryScore score, final JMHAllocRateNorm allocRateNorm) {
        this(JMHPrimaryScoreResults.withoutError(score), JMHAllocRateNormResults.withoutError(allocRateNorm));
    }

    public JMHResults(final JMHPrimaryScore score, final JMHAllocRateNorm allocRateNorm,
            final JMHInstructions instructions) {
        this(
                JMHPrimaryScoreResults.withoutError(score),
                JMHAllocRateNormResults.withoutError(allocRateNorm),
                instructions
        );
    }

    public JMHResults(final JMHPrimaryScore score, final JMHAllocRateNorm allocRateNorm,
            final JMHInstructions instructions, final JMHMemoryLoads memoryLoads,
            final JMHMemoryStores memoryStores) {
        this(
                JMHPrimaryScoreResults.withoutError(score),
                JMHAllocRateNormResults.withoutError(allocRateNorm),
                instructions,
                memoryLoads,
                memoryStores
        );
    }

    public JMHResults(final JMHPrimaryScore score, final JMHAllocRateNorm allocRateNorm, final JMHPerfResults perf) {
        this(JMHPrimaryScoreResults.withoutError(score), JMHAllocRateNormResults.withoutError(allocRateNorm), perf);
    }

    public JMHResults(final JMHPrimaryScoreResults score, final JMHAllocRateNormResults allocRateNorm,
            final JMHPerfResults perf) {
        this.score = score;
        this.allocRateNorm = allocRateNorm;
        this.perf = perf;
    }

    @Override
    public void print(final OutputStream out) {
        final PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
        writer.println("JMH results:");
        writer.flush();
        this.score.print(out);
        this.allocRateNorm.print(out);
        this.perf.print(out);
    }

    @Override
    public List<Artifact<?>> asArtifactRow() {
        final List<Artifact<?>> row = new ArrayList<>();
        row.addAll(this.score.asArtifactRow());
        row.addAll(this.allocRateNorm.asArtifactRow());
        row.addAll(this.perf.asArtifactRow());
        return row;
    }
}
