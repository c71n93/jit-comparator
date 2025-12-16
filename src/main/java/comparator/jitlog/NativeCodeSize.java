package comparator.jitlog;

import comparator.jitlog.jitwatch.JWConfig;
import comparator.jitlog.jitwatch.JWParsedLog;
import comparator.method.TargetMethod;
import java.nio.file.Path;
import java.util.List;
import org.adoptopenjdk.jitwatch.model.Compilation;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.model.MemberSignatureParts;
import org.adoptopenjdk.jitwatch.model.MetaClass;
import org.adoptopenjdk.jitwatch.parser.ParserType;

public final class NativeCodeSize {
    private final TargetMethod targetMethod;
    private final Path jitlog;
    private static final int TIER_FOUR_LEVEL = 4;

    public NativeCodeSize(final TargetMethod targetMethod, final Path jitlog) {
        this.targetMethod = targetMethod;
        this.jitlog = jitlog;
    }

    public int value() {
        final IReadOnlyJITDataModel model = new JWParsedLog(
                ParserType.HOTSPOT, new JWConfig(this.targetMethod), this.jitlog.toFile()
        ).model();
        final IMetaMember member = this.locateMember(model);
        final Compilation compilation = this.latestTierFour(member);
        return compilation.getNativeSize();
    }

    private IMetaMember locateMember(final IReadOnlyJITDataModel model) {
        final MemberSignatureParts msp = this.targetMethod.signatureParts();
        final IMetaMember match = model.findMetaMember(msp);
        if (match != null) {
            return match;
        }
        final MetaClass metaClass = model.getPackageManager().getMetaClass(this.targetMethod.className());
        if (metaClass != null) {
            return metaClass.getMetaMembers().stream()
                    .filter(member -> member.getMemberName().equals(this.targetMethod.methodName())).findFirst()
                    .orElseThrow(() -> new IllegalStateException("Target method not found in JIT log model"));
        }
        throw new IllegalStateException("Target class not present in JIT log model");
    }

    private Compilation latestTierFour(final IMetaMember member) {
        final List<Compilation> compilations = member.getCompilations();
        for (int i = compilations.size() - 1; i >= 0; i--) {
            final Compilation compilation = compilations.get(i);
            if (compilation.getLevel() == NativeCodeSize.TIER_FOUR_LEVEL) {
                return compilation;
            }
        }
        throw new IllegalStateException("No tier 4 compilations found for target method");
    }
}
