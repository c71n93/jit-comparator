package comparator.jitlog;

import comparator.method.TargetMethod;
import comparator.jitlog.test.LogTargetMain;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class NativeCodeSizeTest {
    private static final String TARGET_CLASS = "comparator.jitlog.test.LogTarget";

    @Test
    void returnsNativeSizeForTierFour(@TempDir final Path tempDir) throws Exception {
        final Path logFile = tempDir.resolve("jit-log.xml");
        final TargetMethod target = new TargetMethod(this.testClasses(), NativeCodeSizeTest.TARGET_CLASS, "target");
        this.generateLog(target, logFile, new ArrayList<>());
        final int size = new NativeCodeSize(target, logFile).value();
        Assertions.assertTrue(size > 0, "native code size should be positive");
    }

    @Test
    void failsWhenMethodAbsentInLog(@TempDir final Path tempDir) throws Exception {
        final Path logFile = tempDir.resolve("jit-log.xml");
        final TargetMethod target = new TargetMethod(this.testClasses(), NativeCodeSizeTest.TARGET_CLASS, "target");
        this.generateLog(target, logFile, new ArrayList<>());
        final TargetMethod missing = new TargetMethod(this.testClasses(), NativeCodeSizeTest.TARGET_CLASS, "absent");
        Assertions.assertThrows(IllegalStateException.class, () -> new NativeCodeSize(missing, logFile).value());
    }

    @Test
    void failsWithoutTierFour(@TempDir final Path tempDir) throws Exception {
        final Path logFile = tempDir.resolve("jit-log.xml");
        final TargetMethod target = new TargetMethod(this.testClasses(), NativeCodeSizeTest.TARGET_CLASS, "target");
        final List<String> flags = new ArrayList<>();
        flags.add("-XX:TieredStopAtLevel=1");
        this.generateLog(target, logFile, flags);
        Assertions.assertThrows(IllegalStateException.class, () -> new NativeCodeSize(target, logFile).value());
    }

    private void generateLog(final TargetMethod target, final Path logFile, final List<String> extraFlags)
            throws Exception {
        final List<String> cmd = new ArrayList<>();
        cmd.add(Path.of(System.getProperty("java.home"), "bin", "java").toString());
        cmd.add("-XX:+UnlockDiagnosticVMOptions");
        cmd.add("-XX:+LogCompilation");
        cmd.add("-XX:LogFile=" + logFile.toAbsolutePath());
        cmd.add("-XX:CompileCommand=compileonly," + target.classMethodName());
        cmd.add("-XX:CompileCommand=print," + target.classMethodName());
        cmd.add("-Xbatch");
        cmd.addAll(extraFlags);
        cmd.add("-cp");
        cmd.add(System.getProperty("java.class.path"));
        cmd.add(LogTargetMain.class.getName());
        final Process process = new ProcessBuilder(cmd).start();
        final int exit = process.waitFor();
        if (exit != 0) {
            final String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            final String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            throw new IllegalStateException(
                    "Helper JVM failed: exit=" + exit + ", stdout=" + stdout + ", stderr=" + stderr
            );
        }
        if (!Files.exists(logFile)) {
            throw new IllegalStateException("Helper JVM did not produce a log file");
        }
    }

    private Path testClasses() {
        return Path.of("build", "classes", "java", "test").toAbsolutePath();
    }
}
