public class UseStringBuilderConcatCase {
    private static int render(int seed) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < 48; i++) {
            text.append(seed + i);
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
