package comparator.jitlog.jitwatch;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import org.adoptopenjdk.jitwatch.core.JITWatchConfig;
import org.adoptopenjdk.jitwatch.model.JITDataModel;
import org.adoptopenjdk.jitwatch.model.ParsedClasspath;
import org.adoptopenjdk.jitwatch.model.SplitLog;
import org.adoptopenjdk.jitwatch.parser.ILogParseErrorListener;
import org.adoptopenjdk.jitwatch.parser.ILogParser;
import org.adoptopenjdk.jitwatch.parser.ParserFactory;
import org.adoptopenjdk.jitwatch.parser.ParserType;

public final class JWLogParser implements ILogParser {
    private final ILogParser origin;

    public JWLogParser(final ParserType type, final JITWatchConfig config, final File jitlog) {
        this(type, new SilentListener(), config, jitlog);
    }

    public JWLogParser(final ParserType type, final JITLogParseErrorListener listener, final JITWatchConfig config,
            final File jitlog) {
        this(init(type, listener, config, jitlog));
    }

    private JWLogParser(final ILogParser origin) {
        this.origin = origin;
    }

    @Override
    public void setConfig(final JITWatchConfig config) {
        this.origin.setConfig(config);
    }

    @Override
    public void processLogFile(final File logFile, final ILogParseErrorListener listener) throws IOException {
        this.origin.processLogFile(logFile, listener);
    }

    @Override
    public void processLogFile(final Reader logFileReader, final ILogParseErrorListener listener) throws IOException {
        this.origin.processLogFile(logFileReader, listener);
    }

    @Override
    public SplitLog getSplitLog() {
        return this.origin.getSplitLog();
    }

    @Override
    public void stopParsing() {
        this.origin.stopParsing();
    }

    @Override
    public ParsedClasspath getParsedClasspath() {
        return this.origin.getParsedClasspath();
    }

    @Override
    public JITDataModel getModel() {
        return this.origin.getModel();
    }

    @Override
    public JITWatchConfig getConfig() {
        return this.origin.getConfig();
    }

    @Override
    public void reset() {
        this.origin.reset();
    }

    @Override
    public boolean hasParseError() {
        return this.origin.hasParseError();
    }

    @Override
    public String getVMCommand() {
        return this.origin.getVMCommand();
    }

    @Override
    public void discardParsedLogs() {
        this.origin.discardParsedLogs();
    }

    // TODO: prohibit calling methods that were called here (e.g. setConfig,
    // processLogFile)
    private static ILogParser init(final ParserType type, final JITLogParseErrorListener listener,
            final JITWatchConfig config, final File jitlog) {
        final ILogParser parser = ParserFactory.getParser(type, listener);
        parser.setConfig(config);
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
