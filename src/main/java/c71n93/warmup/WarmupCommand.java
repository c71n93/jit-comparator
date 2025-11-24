package c71n93.warmup;

import c71n93.warmup.jmh.WarmupEntryPoint;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class WarmupCommand {
    private final TargetMethod targetMethod;
    private final Path javaExecutable;
    private final Path logFile;

    public WarmupCommand(final TargetMethod targetMethod) {
        this(targetMethod, Path.of(System.getProperty("java.home"), "bin", "java"), tmpLogFile());
    }

    public WarmupCommand(final TargetMethod targetMethod, final Path javaExecutable, final Path logFile) {
        this.targetMethod = targetMethod;
        this.javaExecutable = javaExecutable;
        this.logFile = logFile;
    }

    public List<String> asList() {
        final List<String> cmd = new ArrayList<>();
        cmd.add(javaExecutable.toString());
        cmd.add("-XX:+UnlockDiagnosticVMOptions");
        cmd.add("-XX:+LogCompilation");
        cmd.add("-XX:LogFile=" + logFile.toAbsolutePath());
        cmd.add("-cp");
        cmd.add(classpath());
        cmd.add(targetMethod.classProperty());
        cmd.add(targetMethod.methodProperty());
        cmd.add(WarmupEntryPoint.class.getName()); // TODO: make Entry class configurable
        return cmd;
    }

    public Path logFile() {
        return logFile;
    }

    private String classpath() {
        final List<String> entries = new ArrayList<>();
        entries.add(targetMethod.classpath().toString());
        entries.add(System.getProperty("java.class.path"));
        return String.join(File.pathSeparator, entries);
    }

    private static Path tmpLogFile() {
        try {
            return Files.createTempFile("jit-log-", ".xml");
        } catch (final IOException e) {
            throw new IllegalStateException("Unable to create JIT log file", e);
        }
    }
}
