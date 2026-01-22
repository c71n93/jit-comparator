package comparator.jitlog;

import comparator.jitlog.test.JITLogFixture;
import comparator.method.TargetMethod;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LogResultsTest {
    private static final String TARGET_CLASS = "comparator.jitlog.test.LogTarget";
    private final JITLogFixture log = new JITLogFixture();

    @Test
    void exposesNativeCodeSizeAsRow(@TempDir final Path tempDir) throws Exception {
        final Path logFile = tempDir.resolve("jit-log.xml");
        final TargetMethod target = new TargetMethod(this.testClasses(), LogResultsTest.TARGET_CLASS, "target");
        this.log.generate(target, logFile);
        final LogResults results = new LogResults(target, logFile);
        final int size = new NativeCodeSize(target, logFile).value();
        Assertions.assertEquals(List.of(String.valueOf(size)), results.asRow(), "Log results should expose code size");
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        results.print(out);
        final String separator = System.lineSeparator();
        final String expected = "Log results:" + separator
                + target.classMethodName() + ": " + size + " bytes" + separator;
        final String output = out.toString(StandardCharsets.UTF_8);
        Assertions.assertEquals(expected, output, "Log results should format output");
    }

    private Path testClasses() {
        return Path.of("build", "classes", "java", "test").toAbsolutePath();
    }
}
