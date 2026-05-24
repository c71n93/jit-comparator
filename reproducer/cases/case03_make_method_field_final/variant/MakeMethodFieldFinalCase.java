import java.lang.reflect.Method;

public class MakeMethodFieldFinalCase {
    private static final Method METHOD = getSum0Method();

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
