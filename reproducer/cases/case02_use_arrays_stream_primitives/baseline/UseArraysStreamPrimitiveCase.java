/*
 * Mechanism: use-arrays-stream.
 * Aggregate provenance: FuzzInput02_ArraysStreamAndBoxing/1-use-arrays-stream-3.
 * Hypothesis: switching from a boxed List stream to a primitive IntStream
 * changes the stream pipeline enough to perturb JIT counters.
 * Expected symptom: instruction/load/store drift in addition to any JMH score
 * movement.
 * Loop note: the outer run() loop is intentionally large enough to keep the
 * coarse comparator run comfortably above ~10 us/op.
 */
import java.util.Arrays;

public class UseArraysStreamPrimitiveCase {
    private static int sumEven(int seed) {
        return Arrays.asList(seed, seed + 1, seed + 2, seed + 3, seed + 4, seed + 5)
            .stream()
            .mapToInt(Integer::intValue)
            .filter(value -> (value & 1) == 0)
            .sum();
    }

    public static int run() {
        int res = 0;
        for (int i = 0; i < 12_000; i++) {
            res += sumEven(i);
        }
        return res;
    }
}
