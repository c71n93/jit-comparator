public class PlainForPlainArrayExample {
    private static final int N = 2_000_000;

    private static int compute(final int x) {
        int y = x * 31;
        y ^= (y >>> 16);
        return y + 7;
    }

    public static long run() {
        final int[] values = new int[N];
        for (int i = 0; i < N; i++) {
            values[i] = i;
        }
        long sum = 0;
        for (final int v : values) {
            sum += compute(v);
        }
        return sum;
    }
}
