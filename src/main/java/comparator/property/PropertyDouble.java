package comparator.property;

/**
 * Double system property accessor.
 */
public final class PropertyDouble extends Property<Double> {
    public PropertyDouble(final String name) {
        super(name, Double::valueOf);
    }
}
