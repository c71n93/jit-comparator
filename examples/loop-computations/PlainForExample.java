import java.util.ArrayList;
import java.util.List;

public class PlainForExample {
    private static final int N = 2_000_000;

    private static int compute(int x) {
        int y = x * 31;
        y ^= (y >>> 16);
        return y + 7;
    }

    public static long run() {
        List<Integer> list = new ArrayList<>(N);
        for (int i = 0; i < N; i++) {
            list.add(i);
        }
        long sum = 0;
        for (int v : list) {
            sum += compute(v);
        }
        return sum;
    }
}
