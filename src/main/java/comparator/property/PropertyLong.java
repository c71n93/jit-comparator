package comparator.property;

/**
 * Long system property accessor.
 */
public final class PropertyLong extends Property<Long> {
    /**
     * PropertyLong.
     *
     * @param name property name
     */
    public PropertyLong(final String name) {
        super(name, Long::valueOf);
    }
}
