package comparator.property;

import java.util.Optional;
import java.util.function.Function;

/**
 * Typed access to a system property with optional conversion.
 *
 * @param <T>
 *            value type produced by the property parser
 */
public class Property<T> {
    private final String name;
    private final Function<String, T> parser;

    /**
     * Creates a property accessor with a custom parser.
     *
     * @param name
     *            system property key
     * @param parser
     *            conversion function from string to target type
     */
    public Property(final String name, final Function<String, T> parser) {
        this.name = name;
        this.parser = parser;
    }

    /**
     * Builds a JVM {@code -Dname=value} argument for this property.
     *
     * @param value
     *            property value to encode
     * @return JVM argument string
     */
    public String asJvmArg(final T value) {
        return "-D" + this.name + "=" + value;
    }

    /**
     * Returns the parsed property value or throws if the property is missing.
     *
     * @return parsed property value
     */
    public T requireValue() {
        final String raw = Optional.ofNullable(System.getProperty(this.name))
                .orElseThrow(() -> new IllegalStateException("Missing property: " + this.name));
        return this.parser.apply(raw);
    }
}
