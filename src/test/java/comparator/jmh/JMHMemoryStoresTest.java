package comparator.jmh;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JMHMemoryStoresTest {
    @Test
    void comparesMemoryStoresUsingAccuracy() {
        final JMHMemoryStores base = new JMHMemoryStores(100.0d, "#/op");
        final JMHMemoryStores within = new JMHMemoryStores(105.0d, "#/op");
        final JMHMemoryStores outside = new JMHMemoryStores(120.0d, "#/op");
        Assertions.assertTrue(base.isSame(within), "Memory stores within threshold should match");
        Assertions.assertFalse(base.isSame(outside), "Memory stores outside threshold should not match");
    }
}
