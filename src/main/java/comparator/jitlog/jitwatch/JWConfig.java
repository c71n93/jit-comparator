package comparator.jitlog.jitwatch;

import comparator.method.Classpath;
import org.adoptopenjdk.jitwatch.core.JITWatchConfig;
import org.adoptopenjdk.jitwatch.model.ParsedClasspath;

public final class JWConfig extends JITWatchConfig {
    public JWConfig(final Classpath classpath) {
        final ParsedClasspath parsed = this.getParsedClasspath();
        for (final String entry : classpath.strings()) {
            parsed.addClassLocation(entry);
        }
    }
}
