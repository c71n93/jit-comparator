package comparator.warmup;

import java.util.Objects;

public final class WarmupConfig {
    // TODO: Current configuration is temporary and for testing. Implement more
    // useful configuration.
    private static final String QUICK_PROPERTY = "warmup.quick";
    private final boolean quick;

    public WarmupConfig(final boolean quick) {
        this.quick = quick;
    }

    public boolean quick() {
        return this.quick;
    }

    public String property() {
        return "-D" + WarmupConfig.QUICK_PROPERTY + "=" + this.quick;
    }

    public static WarmupConfig fromProperty() {
        return new WarmupConfig(Boolean.parseBoolean(WarmupConfig.requiredProperty(WarmupConfig.QUICK_PROPERTY)));
    }

    private static String requiredProperty(final String name) {
        return Objects.requireNonNull(System.getProperty(name), "Missing property: " + name);
    }
}
