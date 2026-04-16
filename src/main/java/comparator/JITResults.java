package comparator;

import comparator.jitlog.LogResults;
import comparator.jmh.results.JMHResults;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * The results of analyzing various JIT artifacts of a specific target method.
 * Combines all other results.
 */
public class JITResults implements Results {
    /** JMH results. */
    private final JMHResults jmh;

    /** JIT log. */
    private final LogResults jitlog;

    /**
     * JITResults.
     *
     * @param jmh JMH results
     * @param jitlog JIT log results
     */
    public JITResults(final JMHResults jmh, final LogResults jitlog) {
        this.jmh = jmh;
        this.jitlog = jitlog;
    }

    // @checkstyle DesignForExtension (8 lines)
    @Override
    public void print(final OutputStream out) {
        final PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
        writer.println("JIT results:");
        this.jmh.print(out);
        this.jitlog.print(out);
        writer.println("---------");
    }

    // TODO: asArtifactRow method is called many times and every time it creates new
    // ArrayList. It may be very expensive and should be optimized.
    // @checkstyle DesignForExtension (8 lines)
    @Override
    public List<Artifact<?>> asArtifactRow() {
        final List<Artifact<?>> row = new ArrayList<>();
        row.addAll(this.jmh.asArtifactRow());
        row.addAll(this.jitlog.asArtifactRow());
        return row;
    }
}
