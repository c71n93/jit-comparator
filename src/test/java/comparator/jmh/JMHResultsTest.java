package comparator.jmh;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JMHResultsTest {
    @Test
    void exposesMetricsAsRow() {
        final JMHResults results = new JMHResults(
                new JMHPrimaryScore(1.5, "us/op"),
                new JMHAllocRateNorm(2.5, "B"),
                Optional.of(new JMHInstructions(3.5, "#/op")),
                Optional.of(new JMHMemoryLoads(4.5, "#/op"))
        );
        Assertions.assertEquals(
                List.of("1.5", "2.5", "3.5", "4.5"),
                results.asRow(),
                "JMH results should expose metric values"
        );
    }

    @Test
    void rendersEmptyPerfMetricsWhenMissing() {
        final JMHResults results = new JMHResults(new JMHPrimaryScore(1.5, "us/op"), new JMHAllocRateNorm(2.5, "B"));
        Assertions.assertEquals(
                List.of("1.5", "2.5", "", ""),
                results.asRow(),
                "Missing perf metrics should be empty"
        );
    }
}
