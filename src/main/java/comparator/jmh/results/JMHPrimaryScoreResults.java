package comparator.jmh.results;

import comparator.Artifact;
import comparator.Results;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Primary-score result pair with the metric value and its relative error.
 */
public final class JMHPrimaryScoreResults implements Results {
    private final JMHPrimaryScore score;
    private final JMHPrimaryScoreError error;

    /**
     * Ctor.
     *
     * @param score
     *            primary score metric
     * @param error
     *            primary score relative error
     */
    public JMHPrimaryScoreResults(final JMHPrimaryScore score, final JMHPrimaryScoreError error) {
        this.score = score;
        this.error = error;
    }

    /**
     * Result pair without a known relative error value.
     *
     * @param score
     *            primary score metric
     * @return primary-score results with missing error represented as
     *         {@link Double#NaN}
     */
    public static JMHPrimaryScoreResults withoutError(final JMHPrimaryScore score) {
        return new JMHPrimaryScoreResults(score, new JMHPrimaryScoreError(Double.NaN));
    }

    @Override
    public void print(final OutputStream out) {
        final PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
        writer.println("- " + this.score.toString());
        writer.println("- " + this.error.toString());
        writer.flush();
    }

    @Override
    public List<Artifact<?>> asArtifactRow() {
        return List.of(this.score, this.error);
    }
}
