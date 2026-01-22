package comparator.jitlog.test;

import comparator.method.TargetMethod;
import comparator.property.PropertyString;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class JITLogFixture {
    private static final PropertyString JAVA_HOME = new PropertyString("java.home");
    private static final PropertyString JAVA_CLASS_PATH = new PropertyString("java.class.path");

    public void generate(final TargetMethod target, final Path logFile) throws Exception {
        this.generate(target, logFile, List.of());
    }

    public void generate(final TargetMethod target, final Path logFile, final List<String> extraFlags)
            throws Exception {
        final List<String> cmd = new ArrayList<>();
        cmd.add(Path.of(JITLogFixture.JAVA_HOME.requireValue(), "bin", "java").toString());
        cmd.add("-XX:+UnlockDiagnosticVMOptions");
        cmd.add("-XX:+LogCompilation");
        cmd.add("-XX:LogFile=" + logFile.toAbsolutePath());
        cmd.add("-XX:CompileCommand=compileonly," + target.classMethodName());
        cmd.add("-XX:CompileCommand=print," + target.classMethodName());
        cmd.add("-Xbatch");
        cmd.addAll(extraFlags);
        cmd.add("-cp");
        cmd.add(JITLogFixture.JAVA_CLASS_PATH.requireValue());
        cmd.add(LogTargetDriver.class.getName());
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
}
