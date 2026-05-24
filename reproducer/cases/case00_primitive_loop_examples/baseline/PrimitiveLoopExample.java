/*
 * Mechanism: primitive loop control examples.
 * Hypothesis: equivalent ways to build, traverse, and transform a small
 * integer collection can produce different optimized code shapes even when
 * they return the same result.
 * Expected symptom: plain-array and indexed-loop variants should stay close to
 * the baseline, while replaceAll and boxed-stream variants are expected to show
 * larger instruction/load/store and native-code-size movement.
 */
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
        long sum = 0;
        for (final int value : list) {
            sum += compute(value);
        }
        return sum;
    }
}
