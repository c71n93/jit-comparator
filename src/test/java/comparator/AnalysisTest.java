package comparator;

import comparator.jmh.fixtures.JMHTarget;
import comparator.jmh.launch.JMHCommand;
import comparator.jmh.launch.JMHConfig;
import comparator.method.TargetMethod;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openjdk.jmh.runner.options.TimeValue;

final class AnalysisTest {
    private static final String TEST_CLASSES = "build/classes/java/test";
    private static final String TARGET_METHOD = "succeed";
    private static final String JITLOG_FILE_NAME = "provided-jit-log.xml";
    private static final String RESULT_FILE_NAME = "provided-jmh-result.json";

    @Test
    void writesJitLogToProvidedPath(@TempDir final Path tempDir) {
        final TargetMethod target = this.targetMethod();
        final Path jitlog = tempDir.resolve(AnalysisTest.JITLOG_FILE_NAME);
        new Analysis(target, jitlog, AnalysisTest.fastConfig()).results();
        Assertions.assertTrue(Files.exists(jitlog), "Analysis should write JIT log to provided path");
    }

    @Test
    void writesJmhResultToProvidedPath(@TempDir final Path tempDir) {
        final TargetMethod target = this.targetMethod();
        final Path jitlog = tempDir.resolve(AnalysisTest.JITLOG_FILE_NAME);
        final Path result = tempDir.resolve(AnalysisTest.RESULT_FILE_NAME);
        new Analysis(target, jitlog, result, AnalysisTest.fastConfig()).results();
        Assertions.assertTrue(Files.exists(result), "Analysis should write JMH result to provided path");
    }

    @Test
    void acceptsPreconfiguredJmhCommand(@TempDir final Path tempDir) {
        final TargetMethod target = this.targetMethod();
        final Path jitlog = tempDir.resolve(AnalysisTest.JITLOG_FILE_NAME);
        final Path result = tempDir.resolve(AnalysisTest.RESULT_FILE_NAME);
        final JMHCommand command = new JMHCommand(
                target,
                jitlog,
                result,
                AnalysisTest.fastConfig()
        );
        new Analysis(command).results();
        Assertions.assertTrue(Files.exists(jitlog), "Analysis should use command-provided JIT log path");
        Assertions.assertTrue(Files.exists(result), "Analysis should use command-provided result path");
    }

    @Test
    void returnsAnalysisCsvRow(@TempDir final Path tempDir) {
        final TargetMethod target = this.targetMethod();
        final Path jitlog = tempDir.resolve(AnalysisTest.JITLOG_FILE_NAME);
        final Path result = tempDir.resolve(AnalysisTest.RESULT_FILE_NAME);
        final List<String> row = new Analysis(
                new JMHCommand(target, jitlog, result, AnalysisTest.fastConfig())
        ).asCsvRow();
        Assertions.assertEquals(6, row.size(), "CSV row should contain target, metrics and output files");
        Assertions.assertEquals(target.classMethodName(), row.get(0), "Target method should be first");
        Assertions.assertTrue(Double.isFinite(Double.parseDouble(row.get(1))), "Primary score should be numeric");
        Assertions.assertTrue(Double.isFinite(Double.parseDouble(row.get(2))), "Alloc rate should be numeric");
        Assertions.assertTrue(Double.isFinite(Double.parseDouble(row.get(3))), "Code size should be numeric");
        Assertions.assertEquals(jitlog.toString(), row.get(4), "JIT log file should be last metrics-adjacent column");
        Assertions.assertEquals(result.toString(), row.get(5), "JMH result file should be final column");
    }

    @Test
    void returnsAnalysisCsvHeader(@TempDir final Path tempDir) {
        final TargetMethod target = this.targetMethod();
        final Path jitlog = tempDir.resolve(AnalysisTest.JITLOG_FILE_NAME);
        final Path result = tempDir.resolve(AnalysisTest.RESULT_FILE_NAME);
        final List<String> header = new Analysis(
                new JMHCommand(target, jitlog, result, AnalysisTest.fastConfig())
        ).headerCsv();
        Assertions.assertEquals(
                List.of(
                        "Target",
                        "JMH primary score, us/op",
                        "Allocations, B/op",
                        "Native code size, B",
                        "JIT log file",
                        "JMH result file"
                ),
                header,
                "CSV header should include target, artifacts and output files"
        );
    }

    private static JMHConfig fastConfig() {
        return new JMHConfig(1, TimeValue.milliseconds(50), 1, TimeValue.milliseconds(50), false);
    }

    private TargetMethod targetMethod() {
        return new TargetMethod(
                Path.of(AnalysisTest.TEST_CLASSES).toAbsolutePath(),
                JMHTarget.class.getName(),
                AnalysisTest.TARGET_METHOD
        );
    }
}
