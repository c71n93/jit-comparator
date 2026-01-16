package comparator.jmh.launch;

import comparator.jmh.launch.benchmark.JMHEntryPoint;
import comparator.method.Classpath;
import comparator.method.TargetMethod;
import comparator.property.PropertyString;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
                Path.of(new PropertyString("java.home").requireValue(), "bin", "java"),
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
                Path.of(new PropertyString("java.home").requireValue(), "bin", "java"),
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
        final List<String> args = new ArrayList<>();
        args.add(this.javaExecutable.toString());
        args.add("-XX:CompileCommand=print," + this.targetMethod.classMethodName());
        args.add("-XX:+UnlockDiagnosticVMOptions");
        args.add("-XX:+LogCompilation");
        args.add("-XX:LogFile=" + this.jitlog.toAbsolutePath());
        args.addAll(this.result.asJvmArgs());
        args.addAll(this.config.asJvmArgs());
        args.addAll(this.targetMethod.asJvmArgs());
        args.add("-cp");
        args.add(this.classpath());
        args.add(JMHEntryPoint.class.getName());
        return args;
    }

    private String classpath() {
        return this.targetMethod.classpath().with(
                new Classpath(new PropertyString("java.class.path").requireValue())
        ).asString();
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
