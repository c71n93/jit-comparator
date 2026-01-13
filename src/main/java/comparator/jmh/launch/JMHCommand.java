package comparator.jmh.launch;

import comparator.jmh.launch.benchmark.JMHEntryPoint;
import comparator.method.TargetMethod;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Launcher that spawns a separate JVM with JIT logging enabled and executes a
 * JMH benchmark for the target method. The fork is needed because
 * {@code -XX:+LogCompilation} can only be provided on JVM startup.
 */
public final class JMHCommand {
    private final TargetMethod targetMethod;
    private final Path javaExecutable;
    private final Path jitlog;
    private final JMHResultFile result;
    private final JMHConfig config;

    public JMHCommand(final TargetMethod targetMethod) {
        this(
                targetMethod,
                Path.of(System.getProperty("java.home"), "bin", "java"),
                JMHCommand.tmpLogFile(),
                new JMHResultFile(JMHCommand.tmpResultFile()),
                new JMHConfig(false)
        );
    }

    public JMHCommand(final TargetMethod targetMethod, final Path javaExecutable, final Path jitlog,
            final JMHResultFile resultFile) {
        this(targetMethod, javaExecutable, jitlog, resultFile, new JMHConfig(false));
    }

    public JMHCommand(final TargetMethod targetMethod, final JMHConfig config) {
        this(
                targetMethod,
                Path.of(System.getProperty("java.home"), "bin", "java"),
                JMHCommand.tmpLogFile(),
                new JMHResultFile(JMHCommand.tmpResultFile()),
                config
        );
    }

    public JMHCommand(final TargetMethod targetMethod, final Path javaExecutable, final Path jitlog,
            final JMHResultFile resultFile, final JMHConfig config) {
        this.targetMethod = targetMethod;
        this.javaExecutable = javaExecutable;
        this.jitlog = jitlog;
        this.result = resultFile;
        this.config = config;
    }

    public JMHOutput run() {
        try {
            final Process process = new ProcessBuilder(this.asList()).start();
            final String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            final String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            final int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IllegalStateException(
                        "JMH run failed with exit code " + exitCode + "\nstdout:\n" + stdout + "\nstderr:\n" + stderr
                );
            }
            return new JMHOutput(this.jitlog, this.result);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("JMH run interrupted", e);
        } catch (final IOException e) {
            throw new IllegalStateException("JMH run failed", e);
        }
    }

    private List<String> asList() {
        return List.of(
                this.javaExecutable.toString(),
                "-XX:CompileCommand=print," + this.targetMethod.classMethodName(),
                "-XX:+UnlockDiagnosticVMOptions",
                "-XX:+LogCompilation",
                "-XX:LogFile=" + this.jitlog.toAbsolutePath(),
                this.result.property(),
                this.config.property(),
                "-cp",
                this.classpath(),
                this.targetMethod.classProperty(),
                this.targetMethod.methodProperty(),
                JMHEntryPoint.class.getName() // TODO: make Entry class configurable
        );
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
            return Files.createTempFile("jmh-result-", ".json");
        } catch (final IOException e) {
            throw new IllegalStateException("Unable to create JMH result file", e);
        }
    }
}
