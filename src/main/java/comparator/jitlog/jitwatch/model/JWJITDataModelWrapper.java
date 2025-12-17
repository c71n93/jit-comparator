package comparator.jitlog.jitwatch.model;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.adoptopenjdk.jitwatch.model.CodeCacheEvent;
import org.adoptopenjdk.jitwatch.model.CompilerThread;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.model.JITEvent;
import org.adoptopenjdk.jitwatch.model.JITStats;
import org.adoptopenjdk.jitwatch.model.MemberSignatureParts;
import org.adoptopenjdk.jitwatch.model.MetaClass;
import org.adoptopenjdk.jitwatch.model.PackageManager;
import org.adoptopenjdk.jitwatch.model.Tag;

public final class JWJITDataModelWrapper implements IReadOnlyJITDataModel {
    private final IReadOnlyJITDataModel origin;

    public JWJITDataModelWrapper(final IReadOnlyJITDataModel origin) {
        this.origin = origin;
    }

    @Override
    public PackageManager getPackageManager() {
        return this.origin.getPackageManager();
    }

    @Override
    public JITStats getJITStats() {
        return this.origin.getJITStats();
    }

    @Override
    public List<JITEvent> getEventListCopy() {
        return this.origin.getEventListCopy();
    }

    @Override
    public List<CodeCacheEvent> getCodeCacheEvents() {
        return this.origin.getCodeCacheEvents();
    }

    @Override
    public List<CompilerThread> getCompilerThreads() {
        return this.origin.getCompilerThreads();
    }

    @Override
    public CompilerThread getCompilerThread(final String threadId) {
        return this.origin.getCompilerThread(threadId);
    }

    @Override
    public CompilerThread createCompilerThread(final String threadId, final String threadName) {
        return this.origin.createCompilerThread(threadId, threadName);
    }

    @Override
    public Tag getEndOfLogTag() {
        return this.origin.getEndOfLogTag();
    }

    @Override
    public int getJDKMajorVersion() {
        return this.origin.getJDKMajorVersion();
    }

    @Override
    public IMetaMember findMetaMember(final MemberSignatureParts msp) {
        return this.origin.findMetaMember(msp);
    }

    public IMetaMember findMetaMember(final Method method) {
        final MemberSignatureParts msp = MemberSignatureParts.fromParts(
                method.getDeclaringClass().getName(),
                method.getName(),
                method.getReturnType().getName(),
                JWJITDataModelWrapper.parameterTypeNames(method)
        );
        return this.origin.findMetaMember(msp);
    }

    public IMetaMember findMetaMemberOrThrow(final Method method) {
        final IMetaMember match = this.findMetaMember(method);
        if (match != null) {
            return match;
        }
        // TODO: not sure that we need this fallback
        // final MetaClass metaClass =
        // this.getPackageManager().getMetaClass(method.getDeclaringClass().getName());
        // if (metaClass != null) {
        // return metaClass.getMetaMembers().stream()
        // .filter(member ->
        // member.getMemberName().equals(method.getName())).findFirst()
        // .orElseThrow(() -> new IllegalStateException("Target method not found in JIT
        // log model"));
        // }
        throw new IllegalStateException("Target is not present in JIT log model");
    }

    @Override
    public MetaClass buildAndGetMetaClass(final Class<?> clazz) {
        return this.origin.buildAndGetMetaClass(clazz);
    }

    @Override
    public long getBaseTimestamp() {
        return this.origin.getBaseTimestamp();
    }

    private static List<String> parameterTypeNames(final Method method) {
        // TODO: Try using stream API here.
        final List<String> names = new ArrayList<>();
        for (final Class<?> param : method.getParameterTypes()) {
            names.add(param.getName());
        }
        return names;
    }
}
