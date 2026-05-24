/*
 * Mechanism: make-fields-and-variables-final.
 * Aggregate provenance: FuzzInput04_FieldMethod/1-make-fields-and-variables-final.
 * Hypothesis: making the reflective Method field final gives HotSpot a more
 * stable field shape around the reflective call path.
 * Expected symptom: counter and generated-code-size movement in the final-field
 * variant.
 * Loop note: the outer run() loop is intentionally large enough to keep the
 * coarse comparator run comfortably above ~10 us/op.
 */
import java.lang.reflect.Method;

public class MakeMethodFieldFinalCase {
    private static Method METHOD = getSum0Method();

    private static Method getSum0Method() {
        try {
            Method method = MakeMethodFieldFinalCase.class.getDeclaredMethod(
                "sum0",
                int.class,
                int.class
            );
            method.setAccessible(true);
            return method;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to initialize METHOD", e);
        }
    }

    private static int sum0(int a, int b) {
        return a + b;
    }

    public static int sum(int a, int b) {
        try {
            return (Integer) METHOD.invoke(null, a, b);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to invoke METHOD", e);
        }
    }

    public static int run() {
        int res = 0;
        for (int i = 0; i < 900; i++) {
            res = sum(res, i);
        }
        return res;
    }
}
