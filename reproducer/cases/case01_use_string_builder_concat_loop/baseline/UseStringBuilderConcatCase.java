/*
 * Mechanism: use-string-builder.
 * Aggregate provenance: FuzzInput09_PushDownAndConcat/1-use-string-builder.
 * Hypothesis: replacing repeated string concatenation with an explicit
 * StringBuilder changes the optimized code shape enough that HotSpot does
 * not converge to the same machine code.
 * Expected symptom: strong allocation drop and supporting counter drift.
 * Loop note: the outer run() loop is intentionally large enough to keep the
 * coarse comparator run comfortably above ~10 us/op.
 */
public class UseStringBuilderConcatCase {
    private static int render(int seed) {
        String text = "";
        for (int i = 0; i < 48; i++) {
            text += seed + i;
        }
        return text.length();
    }

    public static int run() {
        int res = 0;
        for (int i = 0; i < 800; i++) {
            res += render(i);
        }
        return res;
    }
}
