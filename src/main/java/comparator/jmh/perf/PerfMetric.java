package comparator.jmh.perf;

/**
 * Parsed perf-profiler metric value.
 */
public record PerfMetric(double score, String unit) {
}
