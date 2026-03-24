package comparator.jmh.launch.output.perf;

/**
 * Parsed perf-profiler metric value.
 */
public record PerfMetric(double score, String unit) {
}
