/*
 * Mechanism: use-arrays-stream as an inlining-budget cliff.
 * Aggregate provenance: FuzzInput05_MixedInlineArraysAndFinal/
 * 1-inline-method-1/2-use-arrays-stream.
 * Hypothesis: replacing one boxed stream count with Arrays.stream(new int[])
 * should not destabilize unrelated helper inlining, but in practice it can
 * change HotSpot's compile plan enough to perturb whether the run1 -> run0
 * edge is inlined.
 * Expected symptom: instruction/load/store drift with smaller JMH movement; the
 * JIT log may show a run0 inlining divergence.
 * Minimality note: runBody() preserves the original 100-iteration hot loop.
 * The outer run() loop only repeats that shape to keep the coarse comparator
 * above ~10 us/op without rebuilding the whole fuzzed class.
 */
import java.util.Arrays;

public class UseArraysStreamInlineBudgetCase {
    private int sq(int x) {
        return x * x;
    }

    public long run0() {
        int base = 10;
        return Arrays.asList(1, 2, 3).stream().count()
                + Arrays.asList("x", "y").stream().count()
                + sq(base)
                + sq(3)
                + 3;
    }

    public static int run1() {
        return (int) new UseArraysStreamInlineBudgetCase().run0();
    }

    private static int runBody() {
        int res = 0;
        for (int i = 0; i < 100; i++) {
            res += run1();
        }
        return res;
    }

    public static int run() {
        int total = 0;
        for (int r = 0; r < 4; r++) {
            total += runBody();
        }
        return total;
    }
}
