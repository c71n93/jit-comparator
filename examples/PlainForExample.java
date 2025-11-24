public class PlainForExample {
    private static final int N = 2_000_000;

    private static int compute(int x) {
        int y = x * 31;
        y ^= (y >>> 16);
        return y + 7;
    }

    public static long runFor() {
        int[] arr = new int[N];
        for (int i = 0; i < N; i++) {
            arr[i] = i;
        }
        long sum = 0;
        for (int v : arr) {
            sum += compute(v);
        }
        return sum;
    }

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 1000; i++) {
            runFor();
        }
    }
}
