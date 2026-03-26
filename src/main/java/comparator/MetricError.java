package comparator;

/**
 * A scalar artifact representing uncertainty or auxiliary error data for a
 * metric.
 *
 * <p>
 * This interface is introduced so the codebase can distinguish comparable
 * {@link Metric metrics} from their non-comparable error values. At the current
 * stage it is only a marker on top of {@link Artifact}; no extra behavior is
 * added yet.
 * </p>
 */
public interface MetricError extends Artifact<Double> {
}
