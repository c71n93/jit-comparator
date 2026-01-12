package comparator.warmup;

import comparator.warmup.jmh.JMHResultFile;
import java.nio.file.Path;

public final class WarmupOutput {
    private final Path jitlog;
    private final JMHResultFile result;

    public WarmupOutput(final Path jitlog, final JMHResultFile result) {
        this.jitlog = jitlog;
        this.result = result;
    }

    public JMHResults results() {
        return this.result.parsedResult();
    }

    public Path jitlog() {
        return this.jitlog;
    }
}
