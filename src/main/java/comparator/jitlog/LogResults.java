package comparator.jitlog;

import comparator.Results;
import comparator.method.TargetMethod;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class LogResults implements Results {
    private final NativeCodeSize codesize;

    public LogResults(final TargetMethod targetMethod, final Path jitlog) {
        this(new NativeCodeSize(targetMethod, jitlog));
    }

    /**
     * Primary constructor.
     *
     * @param codesize
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
}
