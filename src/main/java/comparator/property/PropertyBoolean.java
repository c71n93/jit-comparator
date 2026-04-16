package comparator.property;

/**
 * Boolean system property accessor.
 */
public final class PropertyBoolean extends Property<Boolean> {
    /**
     * PropertyBoolean.
     *
     * @param name property name
     */
    public PropertyBoolean(final String name) {
        super(name, Boolean::valueOf);
    }
}
