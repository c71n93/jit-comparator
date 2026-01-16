package comparator.property;

/**
 * Boolean system property accessor.
 */
public final class PropertyBoolean extends Property<Boolean> {
    public PropertyBoolean(final String name) {
        super(name, Boolean::valueOf);
    }
}
