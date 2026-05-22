/*
 * Mechanism: plain counted loop vs boxed stream pipeline.
 * External provenance: compiler-comparator/examples/loop-computations/
 * PlainForExample.java -> StreamBoxedExample.java.
 * Hypothesis: replacing a direct accumulation loop with a boxed stream
 * pipeline adds enough library and lambda machinery to change the optimized
 * code shape in a compact, easy-to-explain control example.
 * Expected symptom: higher JMH and supporting instruction/load/store drift in
 * the stream version.
 * Minimality note: this is already the control example itself; only the class
 * name was unified so the comparator can treat the pair as one refactoring.
 */
import java.util.ArrayList;
import java.util.List;

public class LoopComputationsStreamBoxedCase {
    private static final int N = 2_000;

    private static int compute(int x) {
        int y = x * 31;
        y ^= y >>> 16;
        return y + 7;
    }

    public static long run() {
        List<Integer> list = new ArrayList<>(N);
        for (int i = 0; i < N; i++) {
            list.add(i);
        }

        return list.stream()
            .map(LoopComputationsStreamBoxedCase::compute)
            .mapToLong(Integer::longValue)
            .sum();
    }
}
