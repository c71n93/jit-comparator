package comparator.jmh;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JMHMemoryLoadsTest {
    @Test
    void comparesMemoryLoadsUsingAccuracy() {
        final JMHMemoryLoads base = new JMHMemoryLoads(100.0d, "#/op");
        final JMHMemoryLoads within = new JMHMemoryLoads(105.0d, "#/op");
        final JMHMemoryLoads outside = new JMHMemoryLoads(120.0d, "#/op");
        Assertions.assertTrue(base.isSame(within), "Memory loads within threshold should match");
        Assertions.assertFalse(base.isSame(outside), "Memory loads outside threshold should not match");
    }
}
