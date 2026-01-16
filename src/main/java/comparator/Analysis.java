package comparator;

import comparator.jitlog.LogResults;
import comparator.jmh.launch.JMHCommand;
import comparator.jmh.launch.JMHOutput;
import comparator.method.TargetMethod;

/**
 * Analysis of the specified {@link TargetMethod}. Provide JIT-related results
 * for specified target.
 */
public class Analysis {
    private final TargetMethod targetMethod;

    public Analysis(final TargetMethod targetMethod) {
        this.targetMethod = targetMethod;
    }

    /**
     * Executes the JMH run and collects JIT-related results.
     *
     * @return combined JIT and log results for the target method
     */
    public JITResults results() {
        final JMHOutput output = new JMHCommand(this.targetMethod).run();
        return new JITResults(output.results(), new LogResults(this.targetMethod, output.jitlog()));
    }
}
