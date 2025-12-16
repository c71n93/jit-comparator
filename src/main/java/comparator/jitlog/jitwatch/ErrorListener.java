package comparator.jitlog.jitwatch;

import org.adoptopenjdk.jitwatch.model.JITEvent;

/**
 * Listener, that handles only errors.
 */
class ErrorListener implements UniversalJITListener {
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

    @Override
    public void handleErrorEntry(final String entry) {
        // TODO: Implement not silent listener, that will log errors somewhere.
    }

    @Override
    public void handleError(final String title, final String body) {
        throw new IllegalStateException(String.format("%s: %s", title, body));
    }
}
