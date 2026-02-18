package comparator.property;

import java.util.List;

/**
 * Some component that can render its configuration as JVM system property
 * arguments {@code -Dname=value}.
 */
public interface JvmSystemProperties {
    /**
     * JVM system property arguments for this component.
     *
     * @return deterministic list of {@code -Dname=value} arguments
     */
    List<String> asJvmPropertyArgs();
}
