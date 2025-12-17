package comparator.warmup;

import comparator.method.TargetMethod;
import comparator.warmup.jmh.JMHResultFile;
import comparator.warmup.jmh.WarmupEntryPoint;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class WarmupCommand {
    private final TargetMethod targetMethod;
    private final Path javaExecutable;
    private final Path logFile;
    private final JMHResultFile result;
    private final WarmupConfig mode;

    public WarmupCommand(final TargetMethod targetMethod) {
        this(
                targetMethod,
                Path.of(System.getProperty("java.home"), "bin", "java"),
                WarmupCommand.tmpLogFile(),
                new JMHResultFile(WarmupCommand.tmpResultFile()),
                new WarmupConfig(false)
        );
    }

    public WarmupCommand(final TargetMethod targetMethod, final Path javaExecutable, final Path logFile,
            final JMHResultFile resultFile) {
        this(targetMethod, javaExecutable, logFile, resultFile, new WarmupConfig(false));
    }

    public WarmupCommand(final TargetMethod targetMethod, final boolean quick) {
        this(
                targetMethod,
                Path.of(System.getProperty("java.home"), "bin", "java"),
                WarmupCommand.tmpLogFile(),
                new JMHResultFile(WarmupCommand.tmpResultFile()),
                new WarmupConfig(quick)
        );
    }

    public WarmupCommand(final TargetMethod targetMethod, final Path javaExecutable, final Path logFile,
            final JMHResultFile resultFile, final WarmupConfig mode) {
        this.targetMethod = targetMethod;
        this.javaExecutable = javaExecutable;
        this.logFile = logFile;
        this.result = resultFile;
        this.mode = mode;
    }

    public List<String> asList() {
        return List.of(
                this.javaExecutable.toString(),
                "-XX:CompileCommand=print," + this.targetMethod.classMethodName(),
                "-XX:+UnlockDiagnosticVMOptions",
                "-XX:+LogCompilation",
                "-XX:LogFile=" + this.logFile.toAbsolutePath(),
                this.result.property(),
                this.mode.property(),
                "-cp",
                this.classpath(),
                this.targetMethod.classProperty(),
                this.targetMethod.methodProperty(),
                WarmupEntryPoint.class.getName() // TODO: make Entry class configurable
        );
    }

    // TODO: The problem of logFile and resultFile getters is that we can access
    // this files outside even if command was not run yet
    // Figure out how to fix it. Maybe create two different interfaces "Command" and
    // "ExecutedCommand".
    // And return ExecutedCommand from other object that will encapsulate default
    // Command and ProcessBuilder.
    public Path logFile() {
        return this.logFile;
    }

    public JMHResultFile resultFile() {
        return this.result;
    }

    private String classpath() {
        return String.join(
                File.pathSeparator,
                List.of(
                        this.targetMethod.classpath().toString(),
                        System.getProperty("java.class.path")
                )
        );
    }

    private static Path tmpLogFile() {
        try {
            return Files.createTempFile("jit-log-", ".xml");
        } catch (final IOException e) {
            throw new IllegalStateException("Unable to create JIT log file", e);
        }
    }

    private static Path tmpResultFile() {
        try {
            return Files.createTempFile("warmup-result-", ".json");
        } catch (final IOException e) {
            throw new IllegalStateException("Unable to create warmup result file", e);
        }
    }
}
