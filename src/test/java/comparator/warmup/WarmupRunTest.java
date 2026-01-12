package comparator.warmup;

import comparator.method.TargetMethod;
import comparator.warmup.fixtures.WarmupTarget;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

final class WarmupRunTest {
    @Test
    void returnsScoresFromWarmup() {
        final Path classpath = Path.of("build/classes/java/test").toAbsolutePath();
        final TargetMethod target = new TargetMethod(
                classpath, WarmupTarget.class.getName(), "succeed"
        );
        final WarmupOutput output = new WarmupCommand(target, new WarmupConfig(true)).run();
        Assertions.assertTrue(Files.exists(output.jitlog()), "Warmup should produce JIT log file");
        final JMHResults results = output.results();
        Assertions.assertTrue(Double.isFinite(results.score().score()), "Warmup should produce primary score");
        Assertions
                .assertTrue(Double.isFinite(results.allocRateNorm().score()), "Warmup should produce alloc rate norm");
        Assertions.assertFalse(results.score().unit().isEmpty(), "Primary score unit should be present");
        Assertions.assertFalse(results.allocRateNorm().unit().isEmpty(), "Alloc rate norm unit should be present");
    }

    @Test
    void throwsOnWarmupFailure() {
        final Path classpath = Path.of("build/classes/java/test").toAbsolutePath();
        final TargetMethod target = new TargetMethod(
                classpath, WarmupTarget.class.getName(), "fail"
        );
        Assertions
                .assertThrows(
                        IllegalStateException.class,
                        () -> new WarmupCommand(target, new WarmupConfig(true)).run(),
                        "Warmup run should fail on target exception"
                );
    }
}
