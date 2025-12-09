package comparator.jitlog.jitwatch;

import comparator.method.TargetMethod;
import java.util.List;
import org.adoptopenjdk.jitwatch.core.JITWatchConfig;
import org.adoptopenjdk.jitwatch.model.ParsedClasspath;

public final class JWConfig extends JITWatchConfig {
    public JWConfig(final TargetMethod targetMethod) {
        this(List.of(targetMethod.classpath().toString()));
    }

    public JWConfig(final List<String> classpathEntries) {
        final ParsedClasspath classpath = this.getParsedClasspath();
        for (final String entry : classpathEntries) {
            classpath.addClassLocation(entry);
        }
    }
}
