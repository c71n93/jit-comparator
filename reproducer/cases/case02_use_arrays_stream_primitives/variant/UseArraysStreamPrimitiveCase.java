import java.util.Arrays;

public class UseArraysStreamPrimitiveCase {
    private static int sumEven(int seed) {
        return Arrays.stream(new int[] {seed, seed + 1, seed + 2, seed + 3, seed + 4, seed + 5})
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
