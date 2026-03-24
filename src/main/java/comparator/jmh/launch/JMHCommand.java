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
import java.util.UUID;

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
        this(targetMethod, new JMHConfig());
    }

    public JMHCommand(final TargetMethod targetMethod, final JMHConfig config) {
        this(targetMethod, JMHCommand.defaultLogFile(targetMethod), config);
    }

    public JMHCommand(final TargetMethod targetMethod, final Path jitlog) {
        this(targetMethod, jitlog, new JMHConfig());
    }

    public JMHCommand(final TargetMethod targetMethod, final Path jitlog, final JMHConfig config) {
        this(targetMethod, jitlog, JMHCommand.defaultResultFile(targetMethod), config);
    }

    public JMHCommand(final TargetMethod targetMethod, final Path jitlog, final Path resultFile) {
        this(targetMethod, jitlog, resultFile, new JMHConfig());
    }

    public JMHCommand(final TargetMethod targetMethod, final Path jitlog, final Path resultFile,
            final JMHConfig config) {
        this(
                targetMethod,
                Path.of(new PropertyString("java.home").requireValue(), "bin", "java"),
                jitlog,
                resultFile,
                config
        );
    }

    public JMHCommand(final TargetMethod targetMethod, final Path javaExecutable, final Path jitlog,
            final Path resultFile) {
        this(targetMethod, javaExecutable, jitlog, resultFile, new JMHConfig());
    }

    public JMHCommand(final TargetMethod targetMethod, final Path javaExecutable, final Path jitlog,
            final Path resultFile, final JMHConfig config) {
        this.targetMethod = targetMethod;
        this.javaExecutable = javaExecutable;
        this.jitlog = jitlog;
        this.result = new JMHResultFile(resultFile, config.perfEnabled());
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

    public TargetMethod targetMethod() {
        return this.targetMethod;
    }

    public Path jitlog() {
        return this.jitlog;
    }

    public JMHResultFile result() {
        return this.result;
    }

    private List<String> asList() {
        final JMHJitLogFile jitlogFile = new JMHJitLogFile(this.jitlog);
        final List<String> args = new ArrayList<>();
        args.add(this.javaExecutable.toString());
        args.addAll(this.result.asJvmPropertyArgs());
        args.addAll(this.config.asJvmPropertyArgs());
        args.addAll(this.targetMethod.asJvmPropertyArgs());
        args.addAll(jitlogFile.asJvmPropertyArgs());
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

    private static Path defaultLogFile(final TargetMethod targetMethod) {
        return JMHCommand.defaultArtifactDirectory(targetMethod)
                .resolve(JMHCommand.targetClassName(targetMethod) + "-jit-log-" + UUID.randomUUID() + ".xml");
    }

    private static Path defaultResultFile(final TargetMethod targetMethod) {
        return JMHCommand.defaultArtifactDirectory(targetMethod)
                .resolve(JMHCommand.targetClassName(targetMethod) + "-jmh-result-" + UUID.randomUUID() + ".json");
    }

    private static Path defaultArtifactDirectory(final TargetMethod targetMethod) {
        final Path classpath = JMHCommand.firstClasspathEntry(targetMethod);
        final Path directory = classpath.resolve(targetMethod.className().replace('.', '/')).getParent();
        if (directory == null) {
            throw new IllegalStateException(
                    "Unable to determine default artifact directory for " + targetMethod.className()
            );
        }
        return directory;
    }

    private static Path firstClasspathEntry(final TargetMethod targetMethod) {
        return targetMethod.classpath().entries().stream().findFirst().orElseThrow(
                () -> new IllegalStateException("Target classpath is empty")
        );
    }

    private static String targetClassName(final TargetMethod targetMethod) {
        final String className = targetMethod.className();
        return className.substring(className.lastIndexOf('.') + 1).replace('$', '-');
    }
}
