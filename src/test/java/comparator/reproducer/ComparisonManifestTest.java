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
    @Test
    void parsesValidBaselineVariantPair(@TempDir final Path tempDir) throws Exception {
        final Path baseline = Files.createDirectories(tempDir.resolve("baseline"));
        final Path variant = Files.createDirectories(tempDir.resolve("variant"));
        final ComparisonManifest manifest = ComparisonManifest.fromLines(
            List.of(
                String.join(",", ManifestEntry.HEADER),
                "case01,baseline," + baseline + ",Example,run," + tempDir.resolve("baseline.xml")
                    + "," + tempDir.resolve("baseline.json") + ",case01/baseline",
                "case01,variant," + variant + ",Example,run," + tempDir.resolve("variant.xml")
                    + "," + tempDir.resolve("variant.json") + ",case01/variant"
            )
        );
        final ManifestPair pair = manifest.pairs().get("case01");
        Assertions.assertEquals("case01/baseline", pair.baseline().label(), "Baseline label should be parsed");
        Assertions.assertEquals("case01/variant", pair.variant().label(), "Variant label should be parsed");
    }

    @Test
    void rejectsMissingVariant(@TempDir final Path tempDir) throws Exception {
        final Path baseline = Files.createDirectories(tempDir.resolve("baseline"));
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> ComparisonManifest.fromLines(
                List.of(
                    String.join(",", ManifestEntry.HEADER),
                    "case01,baseline," + baseline + ",Example,run," + tempDir.resolve("baseline.xml")
                        + "," + tempDir.resolve("baseline.json") + ",case01/baseline"
                )
            ),
            "One-sided manifest cases should be rejected"
        );
    }
}
