package comparator.jmh.fixtures;

public final class JMHTarget {
    private JMHTarget() {
    }

    public static Object succeed() {
        return "ok";
    }

    public static Object fail() {
        throw new IllegalStateException("failure");
    }
}
