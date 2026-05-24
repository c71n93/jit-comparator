import java.util.ArrayList;
import java.util.List;

public class PrimitiveLoopExample {
    private static final int N = 2_000;

    private static int compute(final int x) {
        int y = x * 31;
        y ^= y >>> 16;
        return y + 7;
    }

    public static long run() {
        final List<Integer> list = new ArrayList<>(N);
        for (int i = 0; i < N; i++) {
            list.add(i);
        }
        return list.stream()
            .map(PrimitiveLoopExample::compute)
            .mapToLong(Integer::longValue)
            .sum();
    }
}
