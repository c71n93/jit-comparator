package comparator.jitlog.jitwatch;

import org.adoptopenjdk.jitwatch.model.JITEvent;

public class SilentListener implements JITLogParseErrorListener {
    @Override
    public void handleJITEvent(final JITEvent event) {
    }

    @Override
    public void handleReadStart() {
    }

    @Override
    public void handleReadComplete() {
    }

    @Override
    public void handleLogEntry(final String entry) {
    }

    // TODO: figure out what is the differences between handleErrorEntry and
    // handleError.
    // As I understand it, failure is OK for the handleErrorEntry.
    @Override
    public void handleErrorEntry(final String entry) {
        // TODO: Implement not silent listener, that will log errors somewhere.
    }

    @Override
    public void handleError(final String title, final String body) {
        // TODO: Should be silent. Find way to separate IJITListener and
        // ILogParseErrorListener interfaces to be able
        // to make one silent, but another not. The main problem is to pass all this
        // mess to JWLogParser ctor.
        throw new IllegalStateException(String.format("%s: %s", title, body));
    }
}
