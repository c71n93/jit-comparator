package comparator.jmh.fixtures;

/** Static JMH target fixture. */
@SuppressWarnings("PMD.ProhibitPublicStaticMethods")
public final class JMHTarget {
    private JMHTarget() {
    }

    /**
     * succeed.
     *
     * @return success marker
     */
    public static Object succeed() {
        return "ok";
    }

    /**
     * fail.
     *
     * @return unreachable result
     */
    public static Object fail() {
        throw new IllegalStateException("failure");
    }
}
