package comparator.jitlog.jitwatch;

import java.io.File;
import java.io.IOException;
import org.adoptopenjdk.jitwatch.core.JITWatchConfig;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.parser.ILogParser;
import org.adoptopenjdk.jitwatch.parser.ParserType;

public class JWParsedLog {
    private final ILogParser parser;

    public JWParsedLog(final ParserType type, final JITWatchConfig config, final File jitlog) {
        this(type, new ErrorListener(), config, jitlog);
    }

    public JWParsedLog(final ParserType type, final UniversalJITListener listener, final JITWatchConfig config,
            final File jitlog) {
        this(init(type, listener, config, jitlog));
    }

    private JWParsedLog(final ILogParser parser) {
        this.parser = parser;
    }

    public IReadOnlyJITDataModel model() {
        return this.parser.getModel();
    }

    private static ILogParser init(final ParserType type, final UniversalJITListener listener,
            final JITWatchConfig config, final File jitlog) {
        final ILogParser parser = new JWLogParser(type, listener, config);
        try {
            parser.processLogFile(jitlog, listener);
        } catch (final IOException e) {
            throw new IllegalStateException("Unable to parse JIT log", e);
        }
        if (parser.hasParseError()) {
            throw new IllegalStateException("JITWatch reported parse errors");
        }
        return parser;
    }
}
