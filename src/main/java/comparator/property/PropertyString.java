package comparator.property;

/**
 * String system property accessor.
 */
public final class PropertyString extends Property<String> {
    public PropertyString(final String name) {
        super(name, value -> value);
    }
}
