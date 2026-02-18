package comparator.jmh;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JMHInstructionsTest {
    @Test
    void comparesInstructionsUsingAccuracy() {
        final JMHInstructions base = new JMHInstructions(100.0d, "#/op");
        final JMHInstructions within = new JMHInstructions(105.0d, "#/op");
        final JMHInstructions outside = new JMHInstructions(120.0d, "#/op");
        Assertions.assertTrue(base.isSame(within), "Instructions within threshold should match");
        Assertions.assertFalse(base.isSame(outside), "Instructions outside threshold should not match");
    }
}
