import java.util.Arrays;

public class UseArraysStreamInlineBudgetCase {
    private int sq(int x) {
        return x * x;
    }

    public long run0() {
        int base = 10;
        return Arrays.stream(new int[] {1, 2, 3}).count()
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
