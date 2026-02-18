package comparator.jmh.launch;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JMHJitLogFileTest {
    @Test
    void rendersJvmPropertyArgs(@TempDir final Path tempDir) {
        final Path jitlog = tempDir.resolve("jit-log.xml");
        final JMHJitLogFile file = new JMHJitLogFile(jitlog);
        Assertions.assertEquals(
                List.of("-Dcomparator.jmh.jitlog.file=" + jitlog.toAbsolutePath()),
                file.asJvmPropertyArgs(),
                "JIT log file should be rendered as JVM system property argument"
        );
    }

    @Test
    void loadsFileFromProperty(@TempDir final Path tempDir) {
        final String key = "comparator.jmh.jitlog.file";
        final String previous = System.getProperty(key);
        final Path jitlog = tempDir.resolve("loaded-jit-log.xml");
        System.setProperty(key, jitlog.toString());
        try {
            Assertions.assertEquals(
                    jitlog.toAbsolutePath(),
                    JMHJitLogFile.fileFromProperty().toAbsolutePath(),
                    "JIT log file should be loaded from JVM property"
            );
        } finally {
            this.restoreProperty(key, previous);
        }
    }

    private void restoreProperty(final String key, final String value) {
        if (value == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, value);
        }
    }
}
