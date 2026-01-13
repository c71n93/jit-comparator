package comparator.jmh.launch;

import comparator.jmh.JMHResults;
import java.nio.file.Path;
import java.util.Objects;

public final class JMHOutput {
    private final Path jitlog;
    private final JMHResultFile result;

    public JMHOutput(final Path jitlog, final JMHResultFile result) {
        this.jitlog = Objects.requireNonNull(jitlog);
        this.result = Objects.requireNonNull(result);
    }

    public JMHResults results() {
        return this.result.parsedResult();
    }

    public Path jitlog() {
        return this.jitlog;
    }
}
