package comparator.jmh;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JMHPrimaryScoreTest {
    private static final String UNIT = "us/op";

    @Test
    void comparesPrimaryScoresUsingAccuracy() {
        final JMHPrimaryScore base = new JMHPrimaryScore(100.0d, JMHPrimaryScoreTest.UNIT);
        final JMHPrimaryScore within = new JMHPrimaryScore(105.0d, JMHPrimaryScoreTest.UNIT);
        final JMHPrimaryScore outside = new JMHPrimaryScore(120.0d, JMHPrimaryScoreTest.UNIT);
        Assertions.assertTrue(base.isSame(within), "Primary score within threshold should match");
        Assertions.assertFalse(base.isSame(outside), "Primary score outside threshold should not match");
    }

    @Test
    void calculatesSymmetricRelativeDifference() {
        final JMHPrimaryScore left = new JMHPrimaryScore(100.0d, JMHPrimaryScoreTest.UNIT);
        final JMHPrimaryScore right = new JMHPrimaryScore(120.0d, JMHPrimaryScoreTest.UNIT);
        final double expected = 2.0d * 20.0d / (100.0d + 120.0d + 1.0e-9);
        Assertions.assertEquals(
                0.0d, left.relativeDifference(left), 1.0e-12,
                "A metric should have zero relative difference with itself"
        );
        Assertions.assertEquals(
                expected, left.relativeDifference(right), 1.0e-12,
                "Relative difference should match sMAPE-like normalization"
        );
        Assertions.assertEquals(
                left.relativeDifference(right), right.relativeDifference(left), 1.0e-12,
                "Relative difference should be symmetric"
        );
    }
}
