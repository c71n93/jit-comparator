package comparator;

import comparator.jitlog.LogResults;
import comparator.jmh.launch.JMHCommand;
import comparator.jmh.launch.JMHOutput;
import comparator.method.TargetMethod;

public class Analysis {
    private final TargetMethod targetMethod;

    public Analysis(final TargetMethod targetMethod) {
        this.targetMethod = targetMethod;
    }

    public JITResults results() {
        final JMHOutput output = new JMHCommand(this.targetMethod).run();
        return new JITResults(output.results(), new LogResults(this.targetMethod, output.jitlog()));
    }
}
