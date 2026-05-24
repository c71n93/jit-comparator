package comparator.reproducer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Strict CSV manifest of reproducer comparison cases.
 */
@SuppressWarnings({ "PMD.ProhibitPublicStaticMethods", "PMD.CyclomaticComplexity" })
public final class ComparisonManifest {
    /** Cases grouped by case identifier. */
    private final Map<String, ManifestCase> cases;

    private ComparisonManifest(final Map<String, ManifestCase> cases) {
        this.cases = Collections.unmodifiableMap(new LinkedHashMap<>(cases));
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
        final Map<String, MutableCase> grouped = new LinkedHashMap<>();
        rows.stream().skip(1).map(ManifestEntry::new).forEach(entry -> {
            final MutableCase pair = grouped.computeIfAbsent(entry.caseId(), key -> new MutableCase());
            pair.add(entry);
        });
        return new ComparisonManifest(ComparisonManifest.freeze(grouped));
    }

    /**
     * @return manifest cases
     */
    public Map<String, ManifestCase> cases() {
        return this.cases;
    }

    private static Map<String, ManifestCase> freeze(final Map<String, MutableCase> grouped) {
        final Map<String, ManifestCase> result = new LinkedHashMap<>();
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
     * Mutable case under construction.
     */
    private static final class MutableCase {
        /** Baseline entry. */
        private ManifestEntry baseline;

        /** Variant entries. */
        private final List<ManifestEntry> variants = new ArrayList<>(0);

        /** Seen roles. */
        private final List<String> roles = new ArrayList<>(0);

        private void add(final ManifestEntry entry) {
            if (this.roles.contains(entry.role())) {
                throw new IllegalArgumentException(
                    "Duplicate manifest role for case: " + entry.caseId() + "/"
                        + entry.role()
                );
            }
            this.roles.add(entry.role());
            if ("baseline".equals(entry.role())) {
                this.baseline = entry;
            } else {
                this.variants.add(entry);
            }
        }

        private ManifestCase freeze(final String caseId) {
            if (this.baseline == null || this.variants.isEmpty()) {
                throw new IllegalArgumentException(
                    "Manifest case must contain one baseline and at least one variant: "
                        + caseId
                );
            }
            return new ManifestCase(this.baseline, this.variants);
        }
    }
}
