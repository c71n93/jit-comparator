import java.util.ArrayList;
import java.util.List;

public class StreamBoxedExample {
    private static final int N = 2_000_000;

    private static int compute(int x) {
        int y = x * 31;
        y ^= (y >>> 16);
        return y + 7;
    }

    public static long runStreamBoxed() {
        List<Integer> list = new ArrayList<>(N);
        for (int i = 0; i < N; i++) {
            list.add(i);
        }
        return list.stream()
                .map(x -> compute(x))
                .mapToLong(Integer::longValue)
                .sum();
    }

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 1000; i++) {
            runStreamBoxed();
        }
    }
}
