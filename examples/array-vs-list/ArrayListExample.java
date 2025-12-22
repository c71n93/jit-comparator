import java.util.ArrayList;
import java.util.List;

public class ArrayListExample {
    private static final int N = 2_000_000;

    private static int compute(final int x) {
        int y = x * 31;
        y ^= (y >>> 16);
        return y + 7;
    }

    public static long runArrayList() {
        final List<Integer> list = new ArrayList<>(N);
        for (int i = 0; i < N; i++) {
            list.add(i);
        }
        long sum = 0;
        for (final int v : list) {
            sum += compute(v);
        }
        return sum;
    }

    public static void main(final String[] args) {
        for (int i = 0; i < 1000; i++) {
            runArrayList();
        }
    }
}
