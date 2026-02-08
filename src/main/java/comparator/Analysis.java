package comparator;

import comparator.jitlog.LogResults;
import comparator.jmh.launch.JMHCommand;
import comparator.jmh.launch.JMHConfig;
import comparator.jmh.launch.JMHOutput;
import comparator.method.TargetMethod;
import java.util.ArrayList;
import java.util.List;

/**
 * Analysis of the specified {@link TargetMethod}.
 */
public class Analysis implements AsRow {
    private final TargetMethod targetMethod;
    private final JMHConfig config;

    /**
     * Ctor.
     *
     * @param targetMethod
     *            target method
     * @param config
     *            JMH execution parameters.
     */
    public Analysis(final TargetMethod targetMethod, final JMHConfig config) {
        this.targetMethod = targetMethod;
        this.config = config;
    }

    /**
     * Ctor.
     *
     * @param targetMethod
     *            target method
     */
    public Analysis(final TargetMethod targetMethod) {
        this(targetMethod, new JMHConfig());
    }

    /**
     * Executes the JMH run and collects JIT-related results.
     *
     * @return combined JIT and log results for the target method
     */
    // TODO: The problem is that this method is called twice: in Analysis.asRow and
    // in Comparison.asCsv
    public JITResults results() {
        final JMHOutput output = new JMHCommand(this.targetMethod, this.config).run();
        return new JITResults(output.results(), new LogResults(this.targetMethod, output.jitlog()));
    }

    @Override
    public List<String> asRow() {
        final List<String> row = new ArrayList<>();
        row.add(this.targetMethod.classMethodName());
        row.addAll(this.results().asRow());
        return row;
    }
}
