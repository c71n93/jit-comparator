package comparator.comparison;

import comparator.Analysis;
import comparator.JITResults;
import java.util.List;

/**
 * Analysis row with predefined CSV values and stubbed JIT results.
 */
final class StubAnalysis extends Analysis {
    private final List<String> row;
    private final JITResults results;

    StubAnalysis(final List<String> row, final StubResults results) {
        super(results.targetMethod());
        this.row = List.copyOf(row);
        this.results = results.asJitResults();
    }

    @Override
    public List<String> asCsvRow() {
        return this.row;
    }

    @Override
    public JITResults results() {
        return this.results;
    }
}
