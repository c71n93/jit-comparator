package comparator.jitlog.test;

/** Static JIT log target fixture. */
@SuppressWarnings("PMD.ProhibitPublicStaticMethods")
public final class LogTarget {
    private LogTarget() {
    }

    /**
     * target.
     *
     * @return deterministic target value
     */
    public static int target() {
        int sum = 0;
        for (int i = 0; i < 1024; i++) {
            sum += i;
        }
        return sum;
    }

    /**
     * absent.
     *
     * @return absent target value
     */
    public static int absent() {
        return 0;
    }
}
