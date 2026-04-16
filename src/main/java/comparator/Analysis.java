package comparator;

import comparator.jitlog.LogResults;
import comparator.jmh.launch.JMHCommand;
import comparator.jmh.launch.JMHConfig;
import comparator.jmh.launch.output.JMHOutput;
import comparator.method.TargetMethod;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Analysis of the specified {@link TargetMethod}.
 */
public class Analysis implements AsCsvRow {
    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(Analysis.class);

    /** JMH command. */
    private final JMHCommand command;

    /** Analysis label. */
    private final String label;

    /** Cached results. */
    private Optional<JITResults> cachedResults;

    /**
     * Ctor.
     *
     * @param command
     *            JMH command
     */
    public Analysis(final JMHCommand command) {
        this(command, command.targetMethod().classMethodName());
    }

    /**
     * Ctor.
     *
     * @param command
     *            JMH command
     * @param label
     *            row label
     */
    public Analysis(final JMHCommand command, final String label) {
        this.command = command;
        this.label = label;
        this.cachedResults = Optional.empty();
    }

    /**
     * Ctor.
     *
     * @param targetMethod
     *            target method
     * @param config
     *            JMH execution parameters.
     */
    public Analysis(final TargetMethod targetMethod, final JMHConfig config) {
        this(targetMethod, config, targetMethod.classMethodName());
    }

    /**
     * Ctor.
     *
     * @param targetMethod
     *            target method
     * @param config
     *            JMH execution parameters.
     * @param label
     *            row label
     */
    public Analysis(final TargetMethod targetMethod, final JMHConfig config, final String label) {
        this(new JMHCommand(targetMethod, config), label);
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
    public Analysis(final TargetMethod targetMethod, final Path jitlog, final Path resultFile) {
        this(targetMethod, jitlog, resultFile, targetMethod.classMethodName());
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
     * @param label
     *            row label
     */
    public Analysis(final TargetMethod targetMethod, final Path jitlog, final Path resultFile, final String label) {
        this(new JMHCommand(targetMethod, jitlog, resultFile), label);
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
    public Analysis(final TargetMethod targetMethod, final Path jitlog, final Path resultFile,
                    final JMHConfig config) {
        this(targetMethod, jitlog, resultFile, config, targetMethod.classMethodName());
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
     * @param label
     *            row label
     */
    // @checkstyle ParameterNumber (5 lines)
    public Analysis(final TargetMethod targetMethod, final Path jitlog, final Path resultFile,
                    final JMHConfig config, final String label) {
        this(new JMHCommand(targetMethod, jitlog, resultFile, config), label);
    }

    /**
     * Ctor.
     *
     * @param targetMethod
     *            target method
     */
    public Analysis(final TargetMethod targetMethod) {
        this(targetMethod, targetMethod.classMethodName());
    }

    /**
     * Ctor.
     *
     * @param targetMethod
     *            target method
     * @param label
     *            row label
     */
    public Analysis(final TargetMethod targetMethod, final String label) {
        this(new JMHCommand(targetMethod), label);
    }

    /**
     * Executes the JMH run and collects JIT-related results.
     *
     * @return combined JIT and log results for the target method
     */
    public JITResults results() {
        if (this.cachedResults.isEmpty()) {
            Analysis.LOG.info("Starting analysis on target: {}", this.label);
            final JMHOutput output = this.command.run();
            this.cachedResults = Optional.of(
                new JITResults(output.results(), new LogResults(this.command.targetMethod(), output.jitlog()))
            );
        }
        return this.cachedResults.orElseThrow();
    }

    // @checkstyle DesignForExtension (9 lines)
    @Override
    public List<String> asCsvRow() {
        final List<String> row = new ArrayList<>();
        row.add(this.label);
        row.addAll(this.results().asCsvRow());
        row.add(this.command.jitlog().toString());
        row.add(this.command.result().toString());
        return row;
    }

    // @checkstyle DesignForExtension (9 lines)
    @Override
    public List<String> headerCsv() {
        final List<String> header = new ArrayList<>();
        header.add("Target");
        header.addAll(this.results().headerCsv());
        header.add("JIT log file");
        header.add("JMH result file");
        return List.copyOf(header);
    }
}
