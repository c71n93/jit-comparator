package comparator.method;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Classpath entries.
 */
public final class Classpath {
    private final List<Path> entries;

    /**
     * Ctor.
     *
     * @param entry
     *            classpath entry
     */
    public Classpath(final Path entry) {
        this(List.of(entry));
    }

    /**
     * Ctor.
     *
     * @param entries
     *            classpath entries
     */
    public Classpath(final List<Path> entries) {
        this.entries = List.copyOf(entries);
    }

    /**
     * Ctor.
     *
     * @param classpath
     *            classpath string using platform path separator
     */
    public Classpath(final String classpath) {
        this(Classpath.parse(classpath));
    }

    /**
     * @return classpath entries as strings
     */
    public List<String> strings() {
        return this.entries.stream().map(Path::toString).toList();
    }

    /**
     * @return classpath entries as URLs
     */
    public List<URL> urls() {
        return this.entries.stream().map(Classpath::toUrl).toList();
    }

    /**
     * @return classpath string using platform path separator
     */
    public String asString() {
        return String.join(File.pathSeparator, this.strings());
    }

    /**
     * @param other
     *            other classpath to append
     * @return combined classpath
     */
    public Classpath with(final Classpath other) {
        final List<Path> merged = new ArrayList<>(this.entries);
        merged.addAll(other.entries);
        return new Classpath(merged);
    }

    private static List<Path> parse(final String classpath) {
        return Arrays.stream(classpath.split(Pattern.quote(File.pathSeparator)))
                .filter(part -> !part.isEmpty())
                .map(Path::of)
                .toList();
    }

    private static URL toUrl(final Path entry) {
        try {
            return entry.toUri().toURL();
        } catch (final MalformedURLException e) {
            throw new IllegalStateException("Unable to convert classpath entry to URL", e);
        }
    }
}
