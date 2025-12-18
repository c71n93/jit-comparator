package comparator.jitlog.jitwatch.model;

import comparator.jitlog.NativeCodeSize;
import java.util.List;
import java.util.Map;
import org.adoptopenjdk.jitwatch.model.Compilation;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.MemberSignatureParts;
import org.adoptopenjdk.jitwatch.model.MetaClass;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyMethod;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeInstruction;
import org.adoptopenjdk.jitwatch.model.bytecode.MemberBytecode;

public final class JWMetaMemberWrapper implements IMetaMember {
    private final IMetaMember origin;

    public JWMetaMemberWrapper(final IMetaMember origin) {
        this.origin = origin;
    }

    @Override
    public MetaClass getMetaClass() {
        return this.origin.getMetaClass();
    }

    @Override
    public MemberBytecode getMemberBytecode() {
        return this.origin.getMemberBytecode();
    }

    @Override
    public List<BytecodeInstruction> getInstructions() {
        return this.origin.getInstructions();
    }

    @Override
    public String toStringUnqualifiedMethodName(final boolean visibilityAndReturnType, final boolean fqParamTypes) {
        return this.origin.toStringUnqualifiedMethodName(visibilityAndReturnType, fqParamTypes);
    }

    @Override
    public String getMemberName() {
        return this.origin.getMemberName();
    }

    @Override
    public String getFullyQualifiedMemberName() {
        return this.origin.getFullyQualifiedMemberName();
    }

    @Override
    public String getAbbreviatedFullyQualifiedMemberName() {
        return this.origin.getAbbreviatedFullyQualifiedMemberName();
    }

    @Override
    public int getModifier() {
        return this.origin.getModifier();
    }

    @Override
    public String getModifierString() {
        return this.origin.getModifierString();
    }

    @Override
    public String getReturnTypeName() {
        return this.origin.getReturnTypeName();
    }

    @Override
    public String[] getParamTypeNames() {
        return this.origin.getParamTypeNames();
    }

    @Override
    public boolean matchesSignature(final MemberSignatureParts msp, final boolean matchTypesExactly) {
        return this.origin.matchesSignature(msp, matchTypesExactly);
    }

    @Override
    public boolean isConstructor() {
        return this.origin.isConstructor();
    }

    @Override
    public String getQueuedAttribute(final String key) {
        return this.origin.getQueuedAttribute(key);
    }

    @Override
    public Map<String, String> getQueuedAttributes() {
        return this.origin.getQueuedAttributes();
    }

    @Override
    public void storeCompilation(final Compilation compilation) {
        this.origin.storeCompilation(compilation);
    }

    @Override
    public String getCompiledAttribute(final String key) {
        return this.origin.getCompiledAttribute(key);
    }

    @Override
    public Map<String, String> getCompiledAttributes() {
        return this.origin.getCompiledAttributes();
    }

    @Override
    public Compilation getCompilationByCompileID(final String compileID) {
        return this.origin.getCompilationByCompileID(compileID);
    }

    @Override
    public Compilation getCompilationByAddress(final AssemblyMethod asmMethod) {
        return this.origin.getCompilationByAddress(asmMethod);
    }

    @Override
    public void setCompiled(final boolean compiled) {
        this.origin.setCompiled(compiled);
    }

    @Override
    public boolean isCompiled() {
        return this.origin.isCompiled();
    }

    @Override
    public void addAssembly(final AssemblyMethod asmMethod) {
        this.origin.addAssembly(asmMethod);
    }

    @Override
    public void setSelectedCompilation(final int index) {
        this.origin.setSelectedCompilation(index);
    }

    @Override
    public Compilation getSelectedCompilation() {
        return this.origin.getSelectedCompilation();
    }

    @Override
    public List<Compilation> getCompilations() {
        return this.origin.getCompilations();
    }

    @Override
    public Compilation getCompilation(final int index) {
        return this.origin.getCompilation(index);
    }

    @Override
    public Compilation getLastCompilation() {
        return this.origin.getLastCompilation();
    }

    // TODO: implement enum for compilation tiers.
    // TODO: Last compilation can be OSR. This means, that result code will definitely be different. We should not include OSR compilation into our analysis.
    public Compilation getLastCompilationOfTier(final int tier) {
        final List<Compilation> compilations = this.getCompilations();
        for (int i = compilations.size() - 1; i >= 0; i--) {
            final Compilation compilation = compilations.get(i);
            if (compilation.getLevel() == tier) {
                return compilation;
            }
        }
        throw new IllegalStateException(String.format("No tier %d compilations found for target method", tier));
    }

    @Override
    public String getSourceMethodSignatureRegEx() {
        return this.origin.getSourceMethodSignatureRegEx();
    }
}
