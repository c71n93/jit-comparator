package comparator.property;

/**
 * Double system property accessor.
 */
public final class PropertyDouble extends Property<Double> {
    /**
     * PropertyDouble.
     *
     * @param name property name
     */
    public PropertyDouble(final String name) {
        super(name, Double::valueOf);
    }
}
