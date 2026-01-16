package comparator.property;

import java.util.List;

/**
 * Describes a component that can render its configuration as JVM {@code -D}
 * arguments. Implementations should return a deterministic list of arguments in
 * the {@code -Dname=value} form, suitable for passing to a JVM process.
 */
public interface Properties {
    /**
     * Returns JVM {@code -D} arguments for this component. The returned list should
     * always be present, may be empty, and should not expose mutable internal
     * state.
     *
     * @return list of JVM arguments
     */
    List<String> asJvmArgs();
}
