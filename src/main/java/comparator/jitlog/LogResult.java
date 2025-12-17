package comparator.jitlog;

import comparator.Result;
import comparator.method.TargetMethod;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class LogResult implements Result {
    private final int nativeSize;

    public LogResult(final TargetMethod targetMethod, final Path jitlog) {
        this(new NativeCodeSize(targetMethod, jitlog).value());
    }

    /**
     * Primary constructor.
     * 
     * @param nativeCodeSize
     */
    private LogResult(final int nativeCodeSize) {
        this.nativeSize = nativeCodeSize;
    }

    @Override
    public void print(final OutputStream out) {
        try {
            out.write(
                    ("native code size: " + this.nativeSize + System.lineSeparator())
                            .getBytes(StandardCharsets.UTF_8)
            );
        } catch (final IOException e) {
            throw new IllegalStateException("Unable to write log results", e);
        }
    }
}
