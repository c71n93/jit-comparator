package comparator.warmup;

import comparator.Results;
import comparator.warmup.jmh.JMHScore;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

/**
 * Stores the results of the forked warmup JVM run: JIT log location and
 * benchmark scores.
 */
public final class WarmupResults implements Results {
    private final Path logPath;
    private final List<JMHScore> scores;

    public WarmupResults(final Path logPath, final List<JMHScore> scores) {
        this.logPath = logPath;
        this.scores = List.copyOf(scores);
    }

    @Override
    public void print(final OutputStream out) {
        final PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
        writer.println("Warmup results:");
        writer.println("JIT log stored at " + this.logPath);
        if (!this.scores.isEmpty()) {
            writer.println("Benchmark scores:");
            for (final JMHScore score : this.scores) {
                writer.println("- " + score.toString());
            }
        }
        writer.flush();
    }

    public Path log() {
        return this.logPath;
    }

    public List<JMHScore> scores() {
        return this.scores;
    }
}
