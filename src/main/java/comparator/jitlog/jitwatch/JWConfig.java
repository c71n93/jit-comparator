package comparator.jitlog.jitwatch;

import comparator.method.Classpath;
import org.adoptopenjdk.jitwatch.core.JITWatchConfig;
import org.adoptopenjdk.jitwatch.model.ParsedClasspath;

/** JWConfig. */
public final class JWConfig extends JITWatchConfig {
    /**
     * JITWatch configuration for a project classpath.
     *
     * @param classpath project classpath
     */
    @SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
    public JWConfig(final Classpath classpath) {
        super();
        final ParsedClasspath parsed = this.getParsedClasspath();
        for (final String entry : classpath.strings()) {
            parsed.addClassLocation(entry);
        }
    }
}
