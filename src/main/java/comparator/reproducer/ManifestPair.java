package comparator.reproducer;

/**
 * Baseline and variant manifest entries for one case.
 */
public final class ManifestPair {
    /** Baseline entry. */
    private final ManifestEntry baseline;

    /** Variant entry. */
    private final ManifestEntry variant;

    /**
     * Ctor.
     *
     * @param baseline
     *            baseline entry
     * @param variant
     *            variant entry
     */
    public ManifestPair(final ManifestEntry baseline, final ManifestEntry variant) {
        this.baseline = baseline;
        this.variant = variant;
    }

    /**
     * @return baseline entry
     */
    public ManifestEntry baseline() {
        return this.baseline;
    }

    /**
     * @return variant entry
     */
    public ManifestEntry variant() {
        return this.variant;
    }
}
