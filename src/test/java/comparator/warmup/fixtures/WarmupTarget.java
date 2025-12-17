package comparator.warmup.fixtures;

public final class WarmupTarget {
    private WarmupTarget() {
    }

    public static Object succeed() {
        return "ok";
    }

    public static Object fail() {
        throw new IllegalStateException("failure");
    }
}
