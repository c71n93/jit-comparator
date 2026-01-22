package comparator.jmh;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JMHResultsTest {
    @Test
    void exposesMetricsAsRow() {
        final JMHResults results = new JMHResults(new JMHPrimaryScore(1.5, "us/op"), new JMHAllocRateNorm(2.5, "B"));
        Assertions.assertEquals(List.of("1.5", "2.5"), results.asRow(), "JMH results should expose metric values");
    }
}
