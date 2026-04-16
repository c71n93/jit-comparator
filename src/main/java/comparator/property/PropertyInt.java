package comparator.property;

/**
 * Integer system property accessor.
 */
public final class PropertyInt extends Property<Integer> {
    /**
     * PropertyInt.
     *
     * @param name property name
     */
    public PropertyInt(final String name) {
        super(name, Integer::valueOf);
    }
}
