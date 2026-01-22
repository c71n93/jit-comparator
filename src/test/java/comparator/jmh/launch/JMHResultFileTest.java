package comparator.jmh.launch;

import comparator.jmh.JMHResults;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JMHResultFileTest {
    @Test
    void parsesMetricsFromJson(@TempDir final Path tempDir) throws Exception {
        final Path result = tempDir.resolve("result.json");
        final String json = "[{\"primaryMetric\":{\"score\":1.1,\"scoreUnit\":\"us/op\"},"
                + "\"secondaryMetrics\":{\"gc.alloc.rate.norm\":{\"score\":2.2,\"scoreUnit\":\"B\"}}}]";
        Files.writeString(result, json, StandardCharsets.UTF_8);
        final JMHResults parsed = new JMHResultFile(result).parsedResult();
        Assertions.assertEquals(List.of("1.1", "2.2"), parsed.asRow(), "JMH result should parse metrics");
    }

    @Test
    void failsWhenPrimaryMetricMissing(@TempDir final Path tempDir) throws Exception {
        final Path result = tempDir.resolve("missing-primary.json");
        final String json = "[{\"secondaryMetrics\":{\"gc.alloc.rate.norm\":{\"score\":2.2,\"scoreUnit\":\"B\"}}}]";
        Files.writeString(result, json, StandardCharsets.UTF_8);
        Assertions.assertThrows(
                IllegalStateException.class,
                () -> new JMHResultFile(result).parsedResult(),
                "Missing primary metric should fail parsing"
        );
    }

    @Test
    void failsWhenAllocRateMissing(@TempDir final Path tempDir) throws Exception {
        final Path result = tempDir.resolve("missing-alloc.json");
        final String json = "[{\"primaryMetric\":{\"score\":1.1,\"scoreUnit\":\"us/op\"}}]";
        Files.writeString(result, json, StandardCharsets.UTF_8);
        Assertions.assertThrows(
                IllegalStateException.class,
                () -> new JMHResultFile(result).parsedResult(),
                "Missing allocation metric should fail parsing"
        );
    }
}
