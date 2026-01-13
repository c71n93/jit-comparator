package comparator.jmh.launch;

import java.util.Objects;

// TODO: implement Property interface, that will consist of two methods: property and fromProperty
public final class JMHConfig {
    // TODO: Current configuration is temporary and for testing. Implement more
    // useful configuration.
    private static final String QUICK_PROPERTY = "jmh.quick";
    private final boolean quick;

    public JMHConfig(final boolean quick) {
        this.quick = quick;
    }

    public boolean quick() {
        return this.quick;
    }

    public String property() {
        return "-D" + JMHConfig.QUICK_PROPERTY + "=" + this.quick;
    }

    public static JMHConfig fromProperty() {
        return new JMHConfig(Boolean.parseBoolean(JMHConfig.requiredProperty(JMHConfig.QUICK_PROPERTY)));
    }

    private static String requiredProperty(final String name) {
        return Objects.requireNonNull(System.getProperty(name), "Missing property: " + name);
    }
}
