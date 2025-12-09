package comparator.jitlog.test;

public final class LogTarget {
    private LogTarget() {
    }

    public static int target() {
        int sum = 0;
        for (int i = 0; i < 1024; i++) {
            sum += i;
        }
        return sum;
    }

    public static int absent() {
        return 0;
    }
}
