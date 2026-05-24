package comparator.reproducer;

import java.util.List;

/**
 * Manifest entries for one reproducer case.
 */
public final class ManifestCase {
    /** Baseline entry. */
    private final ManifestEntry baseline;

    /** Variant entries. */
    private final List<ManifestEntry> variants;

    /**
     * Ctor.
     *
     * @param baseline
     *            baseline entry
     * @param variants
     *            variant entries
     */
    public ManifestCase(final ManifestEntry baseline, final List<ManifestEntry> variants) {
        this.baseline = baseline;
        this.variants = List.copyOf(variants);
    }

    /**
     * @return baseline entry
     */
    public ManifestEntry baseline() {
        return this.baseline;
    }

    /**
     * @return variant entries
     */
    public List<ManifestEntry> variants() {
        return this.variants;
    }
}
