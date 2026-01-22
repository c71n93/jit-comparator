package comparator.jitlog;

import comparator.Results;
import comparator.method.TargetMethod;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

/**
 * Results obtained from JIT log for a single target method. All results
 * extracted for the last compilation in log with the highest tier.
 */
public class LogResults implements Results {
    private final NativeCodeSize codesize;

    /**
     * Ctor.
     *
     * @param targetMethod
     *            target method descriptor
     * @param jitlog
     *            path to JIT compilation log
     */
    public LogResults(final TargetMethod targetMethod, final Path jitlog) {
        this(new NativeCodeSize(targetMethod, jitlog));
    }

    /**
     * Ctor.
     *
     * @param codesize
     *            native code size artifact
     */
    private LogResults(final NativeCodeSize codesize) {
        this.codesize = codesize;
    }

    @Override
    public void print(final OutputStream out) {
        final PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
        writer.println("Log results:");
        writer.println(this.codesize.toString());
    }

    @Override
    public List<String> asRow() {
        return List.of(String.valueOf(this.codesize.value()));
    }
}
