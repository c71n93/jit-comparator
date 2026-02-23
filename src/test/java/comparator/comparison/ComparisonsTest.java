package comparator.comparison;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ComparisonsTest {
    @Test
    void concatenatesComparisonsAsCsv() {
        final CsvComparison first = new CsvComparison(
                new StubAnalysis(
                        List.of("One::run", "1.00", "2", "10", "100", "3"),
                        new StubResults(0.1d)
                ),
                new StubAnalysis(
                        List.of("OneRef::run", "1.10", "2", "11", "110", "3"),
                        new StubResults(0.0d)
                )
        );
        final CsvComparison second = new CsvComparison(
                new StubAnalysis(
                        List.of("Two::run", "2.00", "4", "20", "200", "6"),
                        new StubResults(0.2d)
                ),
                new StubAnalysis(
                        List.of("TwoRef::run", "2.10", "4", "21", "210", "6"),
                        new StubResults(0.0d)
                )
        );
        final CsvComparisons comparisons = new CsvComparisons(first, second);
        final String header = "Target,\"JMH primary score, us/op\",\"Allocations, B\",\"Instructions, #/op\",\"Memory loads, #/op\",\"Native code size, B\","
                + "JIT artifacts mean dissimilarity score,JIT artifacts max dissimilarity score";
        final String rowOne = "One::run,1.00,2,10,100,3,Original,Original";
        final String rowTwo = "OneRef::run,1.10,2,11,110,3,0.1,0.1";
        final String rowThree = "Two::run,2.00,4,20,200,6,Original,Original";
        final String rowFour = "TwoRef::run,2.10,4,21,210,6,0.2,0.2";
        final String expected = String.join(
                System.lineSeparator(),
                header,
                rowOne,
                rowTwo,
                header,
                rowThree,
                rowFour
        );
        Assertions.assertEquals(expected, comparisons.asCsv(), "Comparisons should concatenate rows");
    }

    @Test
    void concatenatesComparisonsAsCsvWithoutPerfColumnsWhenPerfResultsAreMissing() {
        final CsvComparison first = new CsvComparison(
                new StubAnalysis(
                        List.of("One::run", "1.00", "2", "3"),
                        StubResults.withoutPerf(0.1d)
                ),
                new StubAnalysis(
                        List.of("OneRef::run", "1.10", "2", "3"),
                        StubResults.withoutPerf(0.0d)
                )
        );
        final CsvComparison second = new CsvComparison(
                new StubAnalysis(
                        List.of("Two::run", "2.00", "4", "6"),
                        StubResults.withoutPerf(0.2d)
                ),
                new StubAnalysis(
                        List.of("TwoRef::run", "2.10", "4", "6"),
                        StubResults.withoutPerf(0.0d)
                )
        );
        final CsvComparisons comparisons = new CsvComparisons(first, second);
        final String header = "Target,\"JMH primary score, us/op\",\"Allocations, B\",\"Native code size, B\","
                + "JIT artifacts mean dissimilarity score,JIT artifacts max dissimilarity score";
        final String rowOne = "One::run,1.00,2,3,Original,Original";
        final String rowTwo = "OneRef::run,1.10,2,3,0.1,0.1";
        final String rowThree = "Two::run,2.00,4,6,Original,Original";
        final String rowFour = "TwoRef::run,2.10,4,6,0.2,0.2";
        final String expected = String.join(
                System.lineSeparator(),
                header,
                rowOne,
                rowTwo,
                header,
                rowThree,
                rowFour
        );
        Assertions.assertEquals(
                expected,
                comparisons.asCsv(),
                "Comparisons should omit perf columns when perf results are missing"
        );
    }
}
