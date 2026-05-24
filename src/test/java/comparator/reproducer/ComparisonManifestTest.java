package comparator.reproducer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Manifest parser contract tests. */
@SuppressWarnings("PMD.SignatureDeclareThrowsException")
final class ComparisonManifestTest {
    /** Target columns. */
    private static final String TARGET = ",Example,run,";

    @Test
    void parsesValidBaselineVariantCase(@TempDir final Path tempDir) throws Exception {
        final Path baseline = Files.createDirectories(tempDir.resolve("baseline"));
        final Path variant = Files.createDirectories(tempDir.resolve("variant"));
        final ComparisonManifest manifest = ComparisonManifest.fromLines(
            List.of(
                String.join(",", ManifestEntry.HEADER),
                "case01,baseline," + baseline + ComparisonManifestTest.TARGET + tempDir.resolve("baseline.xml")
                    + "," + tempDir.resolve("baseline.json") + ",case01/baseline",
                "case01,variant," + variant + ComparisonManifestTest.TARGET + tempDir.resolve("variant.xml")
                    + "," + tempDir.resolve("variant.json") + ",case01/variant"
            )
        );
        final ManifestCase comparison = manifest.cases().get("case01");
        Assertions.assertEquals("case01/baseline", comparison.baseline().label(), "Baseline label should be parsed");
        Assertions.assertEquals(
            "case01/variant",
            comparison.variants().get(0).label(),
            "Variant label should be parsed"
        );
    }

    @Test
    void parsesMultipleVariants(@TempDir final Path tempDir) throws Exception {
        final Path baseline = Files.createDirectories(tempDir.resolve("baseline"));
        final Path plainArray = Files.createDirectories(tempDir.resolve("plain_array"));
        final Path streamBoxed = Files.createDirectories(tempDir.resolve("stream_boxed"));
        final ComparisonManifest manifest = ComparisonManifest.fromLines(
            List.of(
                String.join(",", ManifestEntry.HEADER),
                "case00,baseline," + baseline + ComparisonManifestTest.TARGET + tempDir.resolve("baseline.xml")
                    + "," + tempDir.resolve("baseline.json") + ",case00/baseline",
                "case00,plain_array," + plainArray + ComparisonManifestTest.TARGET + tempDir.resolve("plain_array.xml")
                    + "," + tempDir.resolve("plain_array.json") + ",case00/plain_array",
                "case00,stream_boxed," + streamBoxed + ComparisonManifestTest.TARGET
                    + tempDir.resolve("stream_boxed.xml")
                    + "," + tempDir.resolve("stream_boxed.json") + ",case00/stream_boxed"
            )
        );
        final ManifestCase comparison = manifest.cases().get("case00");
        Assertions.assertEquals(2, comparison.variants().size(), "All variants should be parsed");
        Assertions.assertEquals(
            List.of("plain_array", "stream_boxed"),
            comparison.variants().stream().map(ManifestEntry::role).toList(),
            "Variant roles should be preserved"
        );
    }

    @Test
    void rejectsMissingVariant(@TempDir final Path tempDir) throws Exception {
        final Path baseline = Files.createDirectories(tempDir.resolve("baseline"));
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> ComparisonManifest.fromLines(
                List.of(
                    String.join(",", ManifestEntry.HEADER),
                    "case01,baseline," + baseline + ComparisonManifestTest.TARGET + tempDir.resolve("baseline.xml")
                        + "," + tempDir.resolve("baseline.json") + ",case01/baseline"
                )
            ),
            "One-sided manifest cases should be rejected"
        );
    }
}
