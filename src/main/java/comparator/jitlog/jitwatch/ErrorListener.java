package comparator.jitlog.jitwatch;

import org.adoptopenjdk.jitwatch.model.JITEvent;

/**
 * Listener, that handles only errors.
 */
final class ErrorListener implements UniversalJITListener {
    @Override
    public void handleJITEvent(final JITEvent event) {
        // Intentionally empty.
    }

    @Override
    public void handleReadStart() {
        // Intentionally empty.
    }

    @Override
    public void handleReadComplete() {
        // Intentionally empty.
    }

    @Override
    public void handleLogEntry(final String entry) {
        // Intentionally empty.
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
