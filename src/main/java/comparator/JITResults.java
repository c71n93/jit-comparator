package comparator;

import comparator.jitlog.LogResults;
import comparator.warmup.WarmupResults;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * The results of analyzing various JIT artifacts of a specific target method.
 * Combines all other results.
 */
public class JITResults implements Results {
    private final WarmupResults warmup;
    private final LogResults jitlog;

    public JITResults(final WarmupResults warmup, final LogResults jitlog) {
        this.warmup = warmup;
        this.jitlog = jitlog;
    }

    @Override
    public void print(final OutputStream out) {
        final PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
        writer.println("JIT results:");
        this.warmup.print(out);
        this.jitlog.print(out);
        writer.println("---------");
    }
}
