package comparator.jmh.launch;

import comparator.property.JvmSystemProperties;
import comparator.property.PropertyString;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * JIT log file property for JMH launcher.
 */
public final class JMHJitLogFile implements JvmSystemProperties {
    private static final PropertyString JIT_LOG_FILE_PROPERTY = new PropertyString("comparator.jmh.jitlog.file");

    private final Path file;

    public JMHJitLogFile(final Path file) {
        this.file = Objects.requireNonNull(file);
    }

    @Override
    public List<String> asJvmPropertyArgs() {
        return List.of(JMHJitLogFile.JIT_LOG_FILE_PROPERTY.asJvmArg(this.file.toAbsolutePath().toString()));
    }

    /**
     * JIT log file path loaded from system properties.
     *
     * @return JIT log file path
     */
    public static Path fileFromProperty() {
        return Path.of(JMHJitLogFile.JIT_LOG_FILE_PROPERTY.requireValue());
    }
}
