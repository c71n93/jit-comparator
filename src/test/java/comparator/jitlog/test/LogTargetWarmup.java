package comparator.jitlog.test;

public final class LogTargetWarmup {
    private LogTargetWarmup() {
    }

    @SuppressWarnings("PMD.SystemPrintln")
    public static void main(final String[] args) {
        int result = 0;
        for (int i = 0; i < 2_000_000; i++) {
            result += LogTarget.target();
        }
        if (result == Long.MIN_VALUE) {
            System.out.print("");
        }
    }
}
