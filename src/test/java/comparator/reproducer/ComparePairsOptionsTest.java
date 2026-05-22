package comparator.reproducer;

import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** CLI option parser contract tests. */
final class ComparePairsOptionsTest {
    @Test
    void parsesManifestAndOutput() {
        final ComparePairsOptions options = ComparePairsOptions.fromArgs(
            "--manifest", "pairs.csv", "--output", "comparisons.csv"
        );
        Assertions.assertEquals(Path.of("pairs.csv"), options.manifest(), "Manifest option should be parsed");
        Assertions.assertEquals(Path.of("comparisons.csv"), options.output(), "Output option should be parsed");
    }

    @Test
    void rejectsMissingOutput() {
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> ComparePairsOptions.fromArgs("--manifest", "pairs.csv"),
            "Missing required options should fail"
        );
    }
}
