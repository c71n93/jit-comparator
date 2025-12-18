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
        final WarmupResults results = new WarmupRun(new WarmupCommand(target, true)).run();
        Assertions.assertTrue(Files.exists(results.log()), "Warmup should produce JIT log file");
        Assertions.assertFalse(results.scores().isEmpty(), "Warmup should produce at least one benchmark score");
        Assertions.assertEquals(
                "comparator.warmup.jmh.WarmupBenchmark.callTarget",
                results.scores().getFirst().benchmark(),
                "Benchmark name should match JMH output"
        );
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
                        () -> new WarmupRun(new WarmupCommand(target, true)).run(),
                        "Warmup run should fail on target exception"
                );
    }
}
