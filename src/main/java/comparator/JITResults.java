package comparator;

import comparator.jitlog.LogResults;
import comparator.jmh.JMHResults;
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
    private final JMHResults jmh;
    private final LogResults jitlog;

    public JITResults(final JMHResults jmh, final LogResults jitlog) {
        this.jmh = jmh;
        this.jitlog = jitlog;
    }

    @Override
    public void print(final OutputStream out) {
        final PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
        writer.println("JIT results:");
        this.jmh.print(out);
        this.jitlog.print(out);
        writer.println("---------");
    }

    // TODO: asArtifactRow method is called many times and every time it creates new
    // ArrayList. It may be very expensive and should be optimized.git
    @Override
    public List<Artifact<?>> asArtifactRow() {
        final List<Artifact<?>> row = new ArrayList<>();
        row.addAll(this.jmh.asArtifactRow());
        row.addAll(this.jitlog.asArtifactRow());
        return row;
    }
}
