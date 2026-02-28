package comparator.comparison;

import comparator.Analysis;
import comparator.JITResults;
import java.util.List;

/**
 * Analysis row with predefined CSV values and stubbed JIT results.
 */
final class StubAnalysis extends Analysis {
    private static final int WIDTH_WITH_PERF = 7;
    private static final int WIDTH_WITHOUT_PERF = 4;
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

    @Override
    public List<String> headerCsv() {
        if (this.row.size() == StubAnalysis.WIDTH_WITH_PERF) {
            return List.of(
                    "Target",
                    "JMH primary score, us/op",
                    "Allocations, B/op",
                    "Instructions, #/op",
                    "Memory loads, #/op",
                    "Memory stores, #/op",
                    "Native code size, B"
            );
        }
        if (this.row.size() == StubAnalysis.WIDTH_WITHOUT_PERF) {
            return List.of(
                    "Target",
                    "JMH primary score, us/op",
                    "Allocations, B/op",
                    "Native code size, B"
            );
        }
        throw new IllegalStateException("Unexpected stub row width: " + this.row.size());
    }
}
