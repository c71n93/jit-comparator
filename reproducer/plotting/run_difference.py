#!/usr/bin/env python3
"""Per-run relative-difference chart for one reproducer case."""

from __future__ import annotations

import argparse
import csv
import math
import os
import sys
import tempfile
from dataclasses import dataclass
from pathlib import Path

MATPLOTLIB_CACHE = Path(tempfile.gettempdir()) / "comparator-matplotlib"
MATPLOTLIB_CACHE.mkdir(parents=True, exist_ok=True)
os.environ.setdefault("MPLCONFIGDIR", str(MATPLOTLIB_CACHE))
os.environ.setdefault("XDG_CACHE_HOME", str(MATPLOTLIB_CACHE / "xdg"))

try:
    import matplotlib

    matplotlib.use("Agg")
    import matplotlib.pyplot as plt
    from matplotlib.ticker import FuncFormatter, MaxNLocator
except ModuleNotFoundError as error:
    MATPLOTLIB_IMPORT_ERROR: ModuleNotFoundError | None = error
else:
    MATPLOTLIB_IMPORT_ERROR = None


REQUIRED_COLUMNS = ("role", "run_index")
ROLES = ("baseline", "variant")


@dataclass(frozen=True)
class MetricSpec:
    """Fixed reproducer metric included in the run-difference chart."""

    label: str
    csv_name: str
    color: str
    marker: str


@dataclass(frozen=True)
class MetricRunDifferences:
    """Ordered relative differences for one metric across reproducer runs."""

    spec: MetricSpec
    run_indices: tuple[int, ...]
    percentages: tuple[float, ...]


class PlottingError(RuntimeError):
    """Fatal run-difference plotting error."""


class AllRunsCsv:
    """Validated all_runs.csv source for one reproducer case."""

    def __init__(self, case_dir: Path) -> None:
        self.case_dir = case_dir
        self.path = case_dir / "all_runs.csv"

    def run_differences(self) -> list[MetricRunDifferences]:
        self._require_case_directory()
        rows = self._rows()
        self._require_roles(rows)
        return [self._differences_for(metric, rows) for metric in METRICS]

    def _require_case_directory(self) -> None:
        if not self.case_dir.is_dir():
            raise PlottingError(f"Case directory does not exist: {self.case_dir}")
        if not self.path.is_file():
            raise PlottingError(f"Missing all_runs.csv: {self.path}")

    def _rows(self) -> list[dict[str, str]]:
        with self.path.open(newline="", encoding="utf-8") as source:
            reader = csv.DictReader(source)
            if reader.fieldnames is None:
                raise PlottingError(f"all_runs.csv is empty: {self.path}")
            missing_required = [name for name in REQUIRED_COLUMNS if name not in reader.fieldnames]
            if missing_required:
                raise PlottingError(
                    f"all_runs.csv is missing required columns "
                    f"{', '.join(missing_required)}: {self.path}"
                )
            for metric in METRICS:
                if metric.csv_name not in reader.fieldnames:
                    raise PlottingError(
                        f"Missing metric column: {metric.csv_name}: {self.case_dir}"
                    )
            return list(reader)

    def _require_roles(self, rows: list[dict[str, str]]) -> None:
        roles = {row.get("role", "") for row in rows}
        for role in ROLES:
            if role not in roles:
                raise PlottingError(f"Missing role {role} in all_runs.csv: {self.case_dir}")

    def _differences_for(
        self,
        metric: MetricSpec,
        rows: list[dict[str, str]],
    ) -> MetricRunDifferences:
        by_role = {
            role: self._values_for(metric, role, rows)
            for role in ROLES
        }
        baseline = by_role["baseline"]
        variant = by_role["variant"]
        self._require_matching_run_indices(metric, baseline, variant)

        run_indices = tuple(sorted(baseline))
        percentages = tuple(
            self._relative_difference(metric, run_index, baseline[run_index], variant[run_index])
            for run_index in run_indices
        )
        return MetricRunDifferences(metric, run_indices, percentages)

    def _values_for(
        self,
        metric: MetricSpec,
        role: str,
        rows: list[dict[str, str]],
    ) -> dict[int, float]:
        values: dict[int, float] = {}
        for row_number, row in enumerate(rows, start=2):
            if row.get("role") != role:
                continue

            run_index = self._positive_run_index(row.get("run_index", ""), metric, role, row_number)
            if run_index in values:
                raise PlottingError(
                    f"Duplicate value for metric {metric.csv_name} "
                    f"role {role} at run {run_index}: {self.case_dir}"
                )
            values[run_index] = self._finite_float(row[metric.csv_name], metric, role, run_index)

        if not values:
            raise PlottingError(
                f"Missing values for metric {metric.csv_name} role {role}: {self.case_dir}"
            )
        return values

    def _positive_run_index(
        self,
        raw: str,
        metric: MetricSpec,
        role: str,
        row_number: int,
    ) -> int:
        try:
            value = int(raw)
        except ValueError as error:
            raise PlottingError(
                f"Invalid run_index for metric {metric.csv_name} "
                f"role {role} at CSV row {row_number}: {raw!r}: {self.case_dir}"
            ) from error
        if value < 1:
            raise PlottingError(
                f"Invalid run_index for metric {metric.csv_name} "
                f"role {role} at CSV row {row_number}: {raw!r}: {self.case_dir}"
            )
        return value

    def _finite_float(self, raw: str, metric: MetricSpec, role: str, run_index: int) -> float:
        try:
            value = float(raw)
        except ValueError as error:
            raise PlottingError(
                f"Metric has non-numeric value for role {role} "
                f"at run {run_index}: {metric.csv_name}: {self.case_dir}"
            ) from error
        if not math.isfinite(value):
            raise PlottingError(
                f"Metric has non-finite value for role {role} "
                f"at run {run_index}: {metric.csv_name}: {self.case_dir}"
            )
        return value

    def _require_matching_run_indices(
        self,
        metric: MetricSpec,
        baseline: dict[int, float],
        variant: dict[int, float],
    ) -> None:
        baseline_runs = set(baseline)
        variant_runs = set(variant)
        if baseline_runs != variant_runs:
            raise PlottingError(
                f"Run index mismatch for metric {metric.csv_name}: "
                f"baseline={sorted(baseline_runs)}, variant={sorted(variant_runs)}: {self.case_dir}"
            )

    def _relative_difference(
        self,
        metric: MetricSpec,
        run_index: int,
        baseline_value: float,
        variant_value: float,
    ) -> float:
        if baseline_value == 0.0:
            raise PlottingError(
                f"Cannot compute relative difference because baseline value is zero "
                f"for metric {metric.csv_name} at run {run_index}: {self.case_dir}"
            )
        return (variant_value - baseline_value) / baseline_value * 100.0


class RunDifferencePlot:
    """PDF line chart for per-run relative differences in one reproducer case."""

    def __init__(
        self,
        case_dir: Path,
        differences: list[MetricRunDifferences],
        output: Path,
    ) -> None:
        self.case_dir = case_dir
        self.differences = differences
        self.output = output

    def save(self) -> None:
        figure, axis = plt.subplots(figsize=(13.8, 7.2))

        for difference in self.differences:
            axis.plot(
                difference.run_indices,
                difference.percentages,
                label=difference.spec.label,
                color=difference.spec.color,
                marker=difference.spec.marker,
                linewidth=2.2,
                markersize=6.8,
                markeredgecolor="white",
                markeredgewidth=1.1,
            )

        axis.axhline(0.0, color="#b8c2cc", linewidth=1.3)
        axis.grid(axis="y", color="#e5eaef", linewidth=1.0)
        axis.grid(axis="x", color="#edf2f7", linewidth=0.8)
        axis.set_axisbelow(True)
        axis.set_xlabel("Run index", fontsize=13)
        axis.set_ylabel("Difference, %", fontsize=13)
        axis.yaxis.set_major_formatter(FuncFormatter(lambda value, _: f"{value:.0f}%"))
        axis.xaxis.set_major_locator(MaxNLocator(integer=True))
        axis.tick_params(axis="both", labelsize=11, colors="#4f6273")
        axis.margins(x=0.02)

        for side in ("top", "right", "left", "bottom"):
            axis.spines[side].set_visible(False)

        axis.set_ylim(*self._limits())
        axis.legend(
            loc="upper left",
            bbox_to_anchor=(1.02, 1.0),
            frameon=False,
            fontsize=12,
            borderaxespad=0.0,
            handlelength=2.6,
        )

        title = (
            f"{self.case_dir.name}\n"
            "Metric difference between original "
            "and modified variant "
            "for each experiment"
        )
        axis.set_title(title, fontsize=16, pad=18)
        figure.subplots_adjust(left=0.08, right=0.78, top=0.84, bottom=0.13)

        self.output.parent.mkdir(parents=True, exist_ok=True)
        figure.savefig(self.output, format="pdf", bbox_inches="tight")
        plt.close(figure)

    def _limits(self) -> tuple[float, float]:
        values = [
            value
            for difference in self.differences
            for value in difference.percentages
        ]
        minimum = min(values + [0.0])
        maximum = max(values + [0.0])
        span = maximum - minimum
        if span == 0.0:
            span = max(abs(maximum), 1.0)

        padding = max(span * 0.12, 1.0)
        lower = minimum - padding
        upper = maximum + padding

        if minimum >= 0.0:
            lower = -padding
        if maximum <= 0.0:
            upper = padding
        return lower, upper


METRICS = (
    MetricSpec("JMH score", "JMH primary score, us/op", "#3f7ee8", "o"),
    MetricSpec("Allocations", "Allocations, B/op", "#dd7433", "o"),
    MetricSpec("Instructions", "Instructions, #/op", "#249b68", "o"),
    MetricSpec("Memory loads", "Memory loads, #/op", "#9b5de5", "o"),
    MetricSpec("Memory stores", "Memory stores, #/op", "#d94f83", "o"),
    MetricSpec("Native code size", "Native code size, B", "#8a6f3f", "o"),
)


def load_run_differences(case_dir: Path) -> list[MetricRunDifferences]:
    return AllRunsCsv(case_dir).run_differences()


def plot_run_differences(
    case_dir: Path,
    differences: list[MetricRunDifferences],
    output: Path,
) -> None:
    RunDifferencePlot(case_dir, differences, output).save()


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Plot per-run relative differences for one reproducer case."
    )
    parser.add_argument("case_dir", help="Path to one reproducer case directory.")
    parser.add_argument(
        "--output",
        help="Output PDF path. Defaults to <case_dir>/run_difference.pdf.",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    case_dir = Path(args.case_dir)
    output = Path(args.output) if args.output else case_dir / "run_difference.pdf"
    try:
        if MATPLOTLIB_IMPORT_ERROR is not None:
            raise PlottingError("Missing Python dependency: matplotlib")
        differences = load_run_differences(case_dir)
        plot_run_differences(case_dir, differences, output)
        return 0
    except PlottingError as error:
        print(str(error), file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
