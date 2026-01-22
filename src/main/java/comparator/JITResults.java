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

    @Override
    public List<String> asRow() {
        final List<String> row = new ArrayList<>();
        row.addAll(this.jmh.asRow());
        row.addAll(this.jitlog.asRow());
        return List.copyOf(row);
    }
}
