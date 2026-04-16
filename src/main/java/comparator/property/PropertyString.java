package comparator.property;

/**
 * String system property accessor.
 */
public final class PropertyString extends Property<String> {
    /**
     * PropertyString.
     *
     * @param name property name
     */
    public PropertyString(final String name) {
        super(name, value -> value);
    }
}
