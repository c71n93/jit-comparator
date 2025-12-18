package comparator;

import comparator.jitlog.LogResults;
import comparator.method.TargetMethod;
import comparator.warmup.WarmupResults;
import comparator.warmup.WarmupRun;

public class Analysis {
    private final TargetMethod targetMethod;

    public Analysis(final TargetMethod targetMethod) {
        this.targetMethod = targetMethod;
    }

    public JITResults results() {
        final WarmupResults warmup = new WarmupRun(this.targetMethod).run();
        return new JITResults(warmup, new LogResults(this.targetMethod, warmup.log()));
    }
}
