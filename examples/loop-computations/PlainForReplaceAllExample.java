import java.util.ArrayList;
import java.util.List;

public class PlainForReplaceAllExample {
    private static final int N = 2_000;

    private static int compute(final int x) {
        int y = x * 31;
        y ^= (y >>> 16);
        return y + 7;
    }

    public static long run() {
        final List<Integer> list = new ArrayList<>(N);
        for (int i = 0; i < N; i++) {
            list.add(i);
        }
        list.replaceAll(PlainForReplaceAllExample::compute);
        long sum = 0;
        for (int v : list) {
            sum += v;
        }
        return sum;
    }
}
