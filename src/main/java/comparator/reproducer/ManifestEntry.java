package comparator.reproducer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Single manifest row for one compared target role.
 */
@SuppressWarnings({ "PMD.DataClass", "PMD.ConstructorOnlyInitializesOrCallOtherConstructors" })
public final class ManifestEntry {
    /** Valid manifest header. */
    public static final List<String> HEADER = List.of(
        "case_id",
        "role",
        "classpath",
        "class_name",
        "method_name",
        "jit_log",
        "jmh_result",
        "label"
    );

    /** Case identifier. */
    private final String caseId;

    /** Case role. */
    private final String role;

    /** Classpath root. */
    private final Path classpath;

    /** Class name. */
    private final String className;

    /** Method name. */
    private final String methodName;

    /** JIT log file. */
    private final Path jitLog;

    /** JMH result file. */
    private final Path jmhResult;

    /** Stable target label. */
    private final String label;

    /**
     * Ctor.
     *
     * @param values
     *            manifest row values
     */
    public ManifestEntry(final List<String> values) {
        if (values.size() != ManifestEntry.HEADER.size()) {
            throw new IllegalArgumentException(
                "Manifest row must contain exactly " + ManifestEntry.HEADER.size()
                    + " columns: " + values
            );
        }
        this.caseId = ManifestEntry.required(values.get(0), "case_id");
        this.role = ManifestEntry.required(values.get(1), "role");
        this.classpath = Path.of(ManifestEntry.required(values.get(2), "classpath"));
        this.className = ManifestEntry.required(values.get(3), "class_name");
        this.methodName = ManifestEntry.required(values.get(4), "method_name");
        this.jitLog = Path.of(ManifestEntry.required(values.get(5), "jit_log"));
        this.jmhResult = Path.of(ManifestEntry.required(values.get(6), "jmh_result"));
        this.label = ManifestEntry.required(values.get(7), "label");
        this.validate();
    }

    /**
     * @return case identifier
     */
    public String caseId() {
        return this.caseId;
    }

    /**
     * @return case role
     */
    public String role() {
        return this.role;
    }

    /**
     * @return classpath root
     */
    public Path classpath() {
        return this.classpath;
    }

    /**
     * @return class name
     */
    public String className() {
        return this.className;
    }

    /**
     * @return method name
     */
    public String methodName() {
        return this.methodName;
    }

    /**
     * @return JIT log file
     */
    public Path jitLog() {
        return this.jitLog;
    }

    /**
     * @return JMH result file
     */
    public Path jmhResult() {
        return this.jmhResult;
    }

    /**
     * @return stable target label
     */
    public String label() {
        return this.label;
    }

    private void validate() {
        if (!"baseline".equals(this.role) && !"variant".equals(this.role)) {
            throw new IllegalArgumentException("Manifest role must be baseline or variant: " + this.role);
        }
        if (!Files.isDirectory(this.classpath)) {
            throw new IllegalArgumentException("Manifest classpath must be an existing directory: " + this.classpath);
        }
    }

    private static String required(final String value, final String column) {
        final String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Manifest column must not be blank: " + column);
        }
        return trimmed;
    }
}
