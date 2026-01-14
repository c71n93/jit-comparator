package comparator.jmh.launch;

import java.nio.file.Path;
import comparator.jmh.JMHResults;

public final class JMHOutput {
    private final Path jitlog;
    private final JMHResultFile result;

    public JMHOutput(final Path jitlog, final JMHResultFile result) {
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
