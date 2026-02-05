package comparator.jmh;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JMHAllocRateNormTest {
    @Test
    void comparesAllocRateUsingAccuracy() {
        final JMHAllocRateNorm base = new JMHAllocRateNorm(100.0d, "B");
        final JMHAllocRateNorm within = new JMHAllocRateNorm(105.0d, "B");
        final JMHAllocRateNorm outside = new JMHAllocRateNorm(120.0d, "B");
        Assertions.assertTrue(base.isSame(within), "Allocation rate within threshold should match");
        Assertions.assertFalse(base.isSame(outside), "Allocation rate outside threshold should not match");
    }
}
