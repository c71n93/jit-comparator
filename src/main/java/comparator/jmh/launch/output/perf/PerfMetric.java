package comparator.jmh.launch.output.perf;

/**
 * Parsed perf-profiler metric value.
 *
 * @param score metric score
 * @param unit metric unit
 */
public record PerfMetric(double score, String unit) {
}
