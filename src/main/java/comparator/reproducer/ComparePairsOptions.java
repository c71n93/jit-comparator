package comparator.reproducer;

import java.nio.file.Path;
import java.util.Arrays;

/**
 * Command line options for reproducer pair comparison.
 */
@SuppressWarnings({ "PMD.ProhibitPublicStaticMethods", "PMD.CyclomaticComplexity" })
public final class ComparePairsOptions {
    /** Help flag. */
    private final boolean help;

    /** Manifest file. */
    private final Path manifest;

    /** Output file. */
    private final Path output;

    private ComparePairsOptions(final boolean help, final Path manifest, final Path output) {
        this.help = help;
        this.manifest = manifest;
        this.output = output;
    }

    /**
     * Parses command line arguments.
     *
     * @param args
     *            command line arguments
     * @return parsed options
     */
    public static ComparePairsOptions fromArgs(final String... args) {
        if (args.length == 1 && "--help".equals(args[0])) {
            return new ComparePairsOptions(true, Path.of("."), Path.of("."));
        }
        Path manifest = Path.of("");
        Path output = Path.of("");
        int index = 0;
        while (index < args.length) {
            final String arg = args[index];
            if ("--manifest".equals(arg)) {
                manifest = ComparePairsOptions.value(args, index + 1, arg);
                index += 2;
            } else if ("--output".equals(arg)) {
                output = ComparePairsOptions.value(args, index + 1, arg);
                index += 2;
            } else {
                throw new IllegalArgumentException("Unknown argument: " + arg + " in " + Arrays.toString(args));
            }
        }
        if (manifest.toString().isEmpty() || output.toString().isEmpty()) {
            throw new IllegalArgumentException("Both --manifest and --output are required");
        }
        return new ComparePairsOptions(false, manifest, output);
    }

    /**
     * @return help flag
     */
    public boolean help() {
        return this.help;
    }

    /**
     * @return manifest file
     */
    public Path manifest() {
        return this.manifest;
    }

    /**
     * @return output file
     */
    public Path output() {
        return this.output;
    }

    private static Path value(final String[] args, final int index, final String option) {
        if (index >= args.length) {
            throw new IllegalArgumentException("Missing value for " + option);
        }
        return Path.of(args[index]);
    }
}
