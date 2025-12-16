package comparator.jitlog.jitwatch;

import org.adoptopenjdk.jitwatch.core.IJITListener;
import org.adoptopenjdk.jitwatch.parser.ILogParseErrorListener;

public interface UniversalJITListener extends IJITListener, ILogParseErrorListener {
}
