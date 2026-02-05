package comparator.jmh;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JMHPrimaryScoreTest {
    @Test
    void comparesPrimaryScoresUsingAccuracy() {
        final JMHPrimaryScore base = new JMHPrimaryScore(100.0d, "us/op");
        final JMHPrimaryScore within = new JMHPrimaryScore(105.0d, "us/op");
        final JMHPrimaryScore outside = new JMHPrimaryScore(120.0d, "us/op");
        Assertions.assertTrue(base.isSame(within), "Primary score within threshold should match");
        Assertions.assertFalse(base.isSame(outside), "Primary score outside threshold should not match");
    }
}
