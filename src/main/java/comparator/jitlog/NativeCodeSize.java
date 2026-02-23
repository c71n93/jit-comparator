package comparator.jitlog;

import comparator.Artifact;
import comparator.jitlog.jitwatch.JWConfig;
import comparator.jitlog.jitwatch.model.JWJITDataModelWrapper;
import comparator.jitlog.jitwatch.JWParsedLog;
import comparator.jitlog.jitwatch.model.JWMetaMemberWrapper;
import comparator.method.TargetMethod;
import java.nio.file.Path;
import java.util.Optional;
import org.adoptopenjdk.jitwatch.model.Compilation;
import org.adoptopenjdk.jitwatch.parser.ParserType;

/**
 * Native code size metric for a target method extracted from a JIT log.
 */
public final class NativeCodeSize implements Artifact<Integer> {
    private final TargetMethod targetMethod;
    private final Path jitlog;
    private Optional<Integer> cached;
    private static final int TIER_LEVEL_4 = 4;

    /**
     * Ctor.
     *
     * @param targetMethod
     *            target method descriptor
     * @param jitlog
     *            path to JIT compilation log
     */
    public NativeCodeSize(final TargetMethod targetMethod, final Path jitlog) {
        this.targetMethod = targetMethod;
        this.jitlog = jitlog;
        this.cached = Optional.empty();
    }

    @Override
    public Integer value() {
        if (this.cached.isEmpty()) {
            this.cached = Optional.of(this.sizeFromJitLog());
        }
        return this.cached.orElseThrow();
    }

    private int sizeFromJitLog() {
        final JWJITDataModelWrapper model = new JWJITDataModelWrapper(
                new JWParsedLog(
                        ParserType.HOTSPOT, new JWConfig(this.targetMethod.classpath()), this.jitlog.toFile()
                ).model()
        );
        final JWMetaMemberWrapper member = new JWMetaMemberWrapper(
                model.findMetaMemberOrThrow(this.targetMethod.method())
        );
        return member.getLastCompilationOfTier(NativeCodeSize.TIER_LEVEL_4)
                .map(Compilation::getNativeSize)
                .orElse(-1);
    }

    @Override
    public String headerCsv() {
        return "Native code size, B";
    }

    @Override
    public String toString() {
        return this.targetMethod.classMethodName() + ": " + this.value() + " bytes";
    }
}
