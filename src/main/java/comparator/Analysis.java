package comparator;

import comparator.jitlog.LogResults;
import comparator.jmh.launch.JMHCommand;
import comparator.jmh.launch.JMHConfig;
import comparator.jmh.launch.JMHOutput;
import comparator.method.TargetMethod;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Analysis of the specified {@link TargetMethod}.
 */
public class Analysis implements AsCsvRow {
    private final JMHCommand command;
    private Optional<JITResults> cachedResults;

    /**
     * Ctor.
     *
     * @param command
     *            JMH command
     */
    public Analysis(final JMHCommand command) {
        this.command = command;
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
        this(new JMHCommand(targetMethod, config));
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
        this(new JMHCommand(targetMethod, jitlog));
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
        this(new JMHCommand(targetMethod, jitlog, config));
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
        this(new JMHCommand(targetMethod, jitlog, resultFile));
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
        this(new JMHCommand(targetMethod, jitlog, resultFile, config));
    }

    /**
     * Ctor.
     *
     * @param targetMethod
     *            target method
     */
    public Analysis(final TargetMethod targetMethod) {
        this(new JMHCommand(targetMethod));
    }

    /**
     * Executes the JMH run and collects JIT-related results.
     *
     * @return combined JIT and log results for the target method
     */
    public JITResults results() {
        if (this.cachedResults.isEmpty()) {
            final JMHOutput output = this.command.run();
            this.cachedResults = Optional.of(
                    new JITResults(output.results(), new LogResults(this.command.targetMethod(), output.jitlog()))
            );
        }
        return this.cachedResults.orElseThrow();
    }

    @Override
    public List<String> asCsvRow() {
        final List<String> row = new ArrayList<>();
        row.add(this.command.targetMethod().classMethodName());
        row.addAll(this.results().asCsvRow());
        row.add(this.command.jitlog().toString());
        row.add(this.command.result().toString());
        return row;
    }

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
