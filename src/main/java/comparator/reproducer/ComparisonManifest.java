package comparator.reproducer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Strict CSV manifest of reproducer comparison pairs.
 */
@SuppressWarnings({ "PMD.ProhibitPublicStaticMethods", "PMD.CyclomaticComplexity" })
public final class ComparisonManifest {
    /** Pairs grouped by case identifier. */
    private final Map<String, ManifestPair> pairs;

    private ComparisonManifest(final Map<String, ManifestPair> pairs) {
        this.pairs = Map.copyOf(pairs);
    }

    /**
     * Reads a manifest file.
     *
     * @param file
     *            manifest file
     * @return parsed manifest
     */
    public static ComparisonManifest fromFile(final Path file) {
        try {
            return ComparisonManifest.fromLines(Files.readAllLines(file, StandardCharsets.UTF_8));
        } catch (final IOException exception) {
            throw new IllegalStateException("Unable to read manifest: " + file, exception);
        }
    }

    /**
     * Reads manifest lines.
     *
     * @param lines
     *            manifest lines
     * @return parsed manifest
     */
    public static ComparisonManifest fromLines(final List<String> lines) {
        final List<List<String>> rows = lines.stream()
            .filter(line -> !line.isBlank())
            .filter(line -> !line.stripLeading().startsWith("#"))
            .map(ComparisonManifest::parseCsvLine)
            .toList();
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Manifest must contain a header");
        }
        if (!ManifestEntry.HEADER.equals(rows.get(0))) {
            throw new IllegalArgumentException("Manifest header must be exactly: " + ManifestEntry.HEADER);
        }
        final Map<String, MutablePair> grouped = new LinkedHashMap<>();
        rows.stream().skip(1).map(ManifestEntry::new).forEach(entry -> {
            final MutablePair pair = grouped.computeIfAbsent(entry.caseId(), key -> new MutablePair());
            pair.add(entry);
        });
        return new ComparisonManifest(ComparisonManifest.freeze(grouped));
    }

    /**
     * @return case pairs
     */
    public Map<String, ManifestPair> pairs() {
        return this.pairs;
    }

    private static Map<String, ManifestPair> freeze(final Map<String, MutablePair> grouped) {
        final Map<String, ManifestPair> result = new LinkedHashMap<>();
        grouped.forEach((caseId, pair) -> result.put(caseId, pair.freeze(caseId)));
        return result;
    }

    private static List<String> parseCsvLine(final String line) {
        final List<String> values = new ArrayList<>();
        final StringBuilder current = new StringBuilder();
        boolean quoted = false;
        int index = 0;
        while (index < line.length()) {
            final char chr = line.charAt(index);
            if (chr == '"') {
                if (quoted && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    current.append(chr);
                    index += 1;
                } else {
                    quoted = !quoted;
                }
            } else if (chr == ',' && !quoted) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(chr);
            }
            index += 1;
        }
        if (quoted) {
            throw new IllegalArgumentException("Unclosed quote in manifest line: " + line);
        }
        values.add(current.toString());
        return List.copyOf(values);
    }

    /**
     * Mutable pair under construction.
     */
    private static final class MutablePair {
        /** Baseline entry. */
        private ManifestEntry baseline;

        /** Variant entry. */
        private ManifestEntry variant;

        private void add(final ManifestEntry entry) {
            if ("baseline".equals(entry.role())) {
                this.baseline = this.requireEmpty(this.baseline, entry);
            } else {
                this.variant = this.requireEmpty(this.variant, entry);
            }
        }

        private ManifestPair freeze(final String caseId) {
            if (this.baseline == null || this.variant == null) {
                throw new IllegalArgumentException(
                    "Manifest case must contain one baseline and one variant: "
                        + caseId
                );
            }
            return new ManifestPair(this.baseline, this.variant);
        }

        private ManifestEntry requireEmpty(final ManifestEntry current, final ManifestEntry next) {
            if (current != null) {
                throw new IllegalArgumentException(
                    "Duplicate manifest role for case: " + next.caseId() + "/"
                        + next.role()
                );
            }
            return next;
        }
    }
}
