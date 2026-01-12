package comparator;

import comparator.jitlog.LogResults;
import comparator.method.TargetMethod;
import comparator.warmup.WarmupCommand;
import comparator.warmup.WarmupOutput;

public class Analysis {
    private final TargetMethod targetMethod;

    public Analysis(final TargetMethod targetMethod) {
        this.targetMethod = targetMethod;
    }

    public JITResults results() {
        final WarmupOutput output = new WarmupCommand(this.targetMethod).run();
        return new JITResults(output.results(), new LogResults(this.targetMethod, output.jitlog()));
    }
}
