package comparator.jmh.launch.benchmark;

import comparator.method.TargetMethod;
import java.lang.reflect.Method;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/**
 * JMH state object that looks up the user method once per trial. Keeping all of
 * the reflection in setup method keeps the benchmark itself straightforward.
 */
@State(Scope.Thread)
public class JMHTargetState {
    private Method targetMethod;

    @Setup(Level.Trial)
    public void loadTarget() throws Exception {
        this.targetMethod = TargetMethod.runnableFromProperties();
    }

    public Object invoke() throws Exception {
        return this.targetMethod.invoke(null);
    }
}
