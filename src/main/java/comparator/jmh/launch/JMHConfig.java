package comparator.jmh.launch;

import comparator.property.Properties;
import comparator.property.PropertyBoolean;
import java.util.List;

public final class JMHConfig implements Properties {
    // TODO: Current configuration is temporary and for testing. Implement more
    // useful configuration.
    private static final PropertyBoolean QUICK_PROPERTY = new PropertyBoolean("jmh.quick");
    private final boolean quick;

    public JMHConfig(final boolean quick) {
        this.quick = quick;
    }

    public boolean quick() {
        return this.quick;
    }

    // TODO: find a way to add fromProperties static method to the contract of
    // Properties interface.
    public static JMHConfig fromProperties() {
        return new JMHConfig(QUICK_PROPERTY.requireValue());
    }

    @Override
    public List<String> asJvmArgs() {
        return List.of(JMHConfig.QUICK_PROPERTY.asJvmArg(this.quick));
    }
}
