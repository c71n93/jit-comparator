package comparator.jmh.launch.output;

import comparator.jmh.results.JMHResults;
import java.nio.file.Path;

/** JMHOutput. */
public final class JMHOutput {
    /** JIT log. */
    private final Path jitlog;

    /** Result file. */
    private final JMHResultFile result;

    /**
     * JMHOutput.
     *
     * @param jitlog JIT log path
     * @param result JMH result file
     */
    public JMHOutput(final Path jitlog, final JMHResultFile result) {
        this.jitlog = jitlog;
        this.result = result;
    }

    /**
     * results.
     *
     * @return parsed JMH results
     */
    public JMHResults results() {
        return this.result.parsedResult();
    }

    /**
     * jitlog.
     *
     * @return JIT log path
     */
    public Path jitlog() {
        return this.jitlog;
    }
}
