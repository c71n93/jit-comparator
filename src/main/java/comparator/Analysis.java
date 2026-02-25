package comparator;

import comparator.jitlog.LogResults;
import comparator.jmh.launch.JMHCommand;
import comparator.jmh.launch.JMHConfig;
import comparator.jmh.launch.JMHOutput;
import comparator.jmh.launch.JMHResultFile;
import comparator.method.TargetMethod;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Analysis of the specified {@link TargetMethod}.
 */
public class Analysis implements AsCsvRow {
    private final TargetMethod targetMethod;
    private final JMHConfig config;
    private final Path jitlog;
    private final JMHResultFile result;
    private Optional<JITResults> cachedResults;

    /**
     * Ctor.
     *
     * @param targetMethod
     *            target method
     * @param config
     *            JMH execution parameters.
     */
    public Analysis(final TargetMethod targetMethod, final JMHConfig config) {
        this(targetMethod, Analysis.tmpLogFile(), new JMHResultFile(Analysis.tmpResultFile()), config);
    }

    /**
     * Ctor.
     *
     * @param targetMethod
     *            target method
     * @param jitlog
     *            JIT log output file
     */
    public Analysis(final TargetMethod targetMethod, final Path jitlog) {
        this(targetMethod, jitlog, new JMHResultFile(Analysis.tmpResultFile()), new JMHConfig());
    }

    /**
     * Ctor.
     *
     * @param targetMethod
     *            target method
     * @param jitlog
     *            JIT log output file
     * @param config
     *            JMH execution parameters.
     */
    public Analysis(final TargetMethod targetMethod, final Path jitlog, final JMHConfig config) {
        this(targetMethod, jitlog, new JMHResultFile(Analysis.tmpResultFile()), config);
    }

    /**
     * Ctor.
     *
     * @param targetMethod
     *            target method
     * @param jitlog
     *            JIT log output file
     * @param resultFile
     *            JMH result output file
     */
    public Analysis(final TargetMethod targetMethod, final Path jitlog, final JMHResultFile resultFile) {
        this(targetMethod, jitlog, resultFile, new JMHConfig());
    }

    /**
     * Ctor.
     *
     * @param targetMethod
     *            target method
     * @param jitlog
     *            JIT log output file
     * @param resultFile
     *            JMH result output file
     * @param config
     *            JMH execution parameters.
     */
    public Analysis(final TargetMethod targetMethod, final Path jitlog, final JMHResultFile resultFile,
            final JMHConfig config) {
        this.targetMethod = targetMethod;
        this.config = config;
        this.jitlog = jitlog;
        this.result = resultFile;
        this.cachedResults = Optional.empty();
    }

    /**
     * Ctor.
     *
     * @param targetMethod
     *            target method
     */
    public Analysis(final TargetMethod targetMethod) {
        this(targetMethod, new JMHConfig());
    }

    /**
     * Executes the JMH run and collects JIT-related results.
     *
     * @return combined JIT and log results for the target method
     */
    public JITResults results() {
        if (this.cachedResults.isEmpty()) {
            final JMHOutput output = new JMHCommand(this.targetMethod, this.jitlog, this.result, this.config).run();
            this.cachedResults = Optional.of(
                    new JITResults(output.results(), new LogResults(this.targetMethod, output.jitlog()))
            );
        }
        return this.cachedResults.orElseThrow();
    }

    @Override
    public List<String> asCsvRow() {
        final List<String> row = new ArrayList<>();
        row.add(this.targetMethod.classMethodName());
        row.addAll(this.results().asCsvRow());
        return row;
    }

    @Override
    public List<String> headerCsv() {
        final List<String> header = new ArrayList<>();
        header.add("Target");
        header.addAll(this.results().headerCsv());
        return List.copyOf(header);
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
