package comparator.warmup;

import comparator.method.TargetMethod;
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

    public WarmupCommand(final TargetMethod targetMethod) {
        this(targetMethod, Path.of(System.getProperty("java.home"), "bin", "java"), WarmupCommand.tmpLogFile());
    }

    public WarmupCommand(final TargetMethod targetMethod, final Path javaExecutable, final Path logFile) {
        this.targetMethod = targetMethod;
        this.javaExecutable = javaExecutable;
        this.logFile = logFile;
    }

    public List<String> asList() {
        return List.of(
                this.javaExecutable.toString(),
                "-XX:CompileCommand=print," + this.targetMethod.classMethodName(),
                "-XX:+UnlockDiagnosticVMOptions",
                "-XX:+LogCompilation",
                "-XX:LogFile=" + this.logFile.toAbsolutePath(),
                "-cp",
                this.classpath(),
                this.targetMethod.classProperty(),
                this.targetMethod.methodProperty(),
                WarmupEntryPoint.class.getName() // TODO: make Entry class configurable
        );
    }

    public Path logFile() {
        return this.logFile;
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
}
