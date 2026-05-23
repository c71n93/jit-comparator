#!/usr/bin/env python3
"""Mean relative-difference chart for one reproducer case."""

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
    from matplotlib.ticker import FuncFormatter
except ModuleNotFoundError as error:
    MATPLOTLIB_IMPORT_ERROR: ModuleNotFoundError | None = error
else:
    MATPLOTLIB_IMPORT_ERROR = None


REQUIRED_COLUMNS = ("role", "metric", "count", "mean", "stdev")
ROLES = ("baseline", "variant")


@dataclass(frozen=True)
class MetricSpec:
    """Fixed reproducer metric included in the mean-difference chart."""

    label: str
    csv_name: str


@dataclass(frozen=True)
class RoleSummary:
    """Aggregated measurement row for one metric role."""

    role: str
    count: int
    mean: float
    stdev: float

    @property
    def cv_percent(self) -> float:
        return self.stdev / self.mean * 100.0


@dataclass(frozen=True)
class MetricSummary:
    """Mean comparison for one metric across baseline and variant roles."""

    spec: MetricSpec
    baseline: RoleSummary
    variant: RoleSummary

    @property
    def count(self) -> int:
        return self.baseline.count

    @property
    def relative_difference_percent(self) -> float:
        return (self.variant.mean - self.baseline.mean) / self.baseline.mean * 100.0

    @property
    def cv_label(self) -> str:
        return (
            f"{self.spec.label}\n"
            f"CV(orig): {self.baseline.cv_percent:.2f}%\n"
            f"CV(ref): {self.variant.cv_percent:.2f}%"
        )


class PlottingError(RuntimeError):
    """Fatal mean-difference plotting error."""


class SummaryCsv:
    """Validated summary.csv source for one reproducer case."""

    def __init__(self, case_dir: Path) -> None:
        self.case_dir = case_dir
        self.path = case_dir / "summary.csv"

    def metric_summaries(self) -> list[MetricSummary]:
        self._require_case_directory()
        rows = self._rows()
        summaries = [self._summary_for(metric, rows) for metric in METRICS]
        self._require_same_count(summaries)
        return summaries

    def _require_case_directory(self) -> None:
        if not self.case_dir.is_dir():
            raise PlottingError(f"Case directory does not exist: {self.case_dir}")
        if not self.path.is_file():
            raise PlottingError(f"Missing summary.csv: {self.path}")

    def _rows(self) -> list[dict[str, str]]:
        with self.path.open(newline="", encoding="utf-8") as source:
            reader = csv.DictReader(source)
            if reader.fieldnames is None:
                raise PlottingError(f"summary.csv is empty: {self.path}")
            missing = [name for name in REQUIRED_COLUMNS if name not in reader.fieldnames]
            if missing:
                raise PlottingError(
                    f"summary.csv is missing required columns {', '.join(missing)}: {self.path}"
                )
            return list(reader)

    def _summary_for(self, metric: MetricSpec, rows: list[dict[str, str]]) -> MetricSummary:
        by_role = {
            role: self._single_role_summary(metric, role, rows)
            for role in ROLES
        }
        baseline = by_role["baseline"]
        variant = by_role["variant"]
        if baseline.count != variant.count:
            raise PlottingError(
                f"Inconsistent run count in summary.csv for metric {metric.csv_name}: {self.case_dir}"
            )
        if baseline.mean == 0.0:
            raise PlottingError(
                f"Cannot compute relative difference because baseline mean is zero "
                f"for metric {metric.csv_name}: {self.case_dir}"
            )
        if variant.mean == 0.0:
            raise PlottingError(
                f"Cannot compute CV because variant mean is zero for metric {metric.csv_name}: {self.case_dir}"
            )
        return MetricSummary(metric, baseline, variant)

    def _single_role_summary(
        self,
        metric: MetricSpec,
        role: str,
        rows: list[dict[str, str]],
    ) -> RoleSummary:
        matches = [
            row
            for row in rows
            if row.get("role") == role and row.get("metric") == metric.csv_name
        ]
        if len(matches) == 0:
            raise PlottingError(f"Missing metric for role {role}: {metric.csv_name}: {self.case_dir}")
        if len(matches) > 1:
            raise PlottingError(f"Duplicate metric for role {role}: {metric.csv_name}: {self.case_dir}")
        row = matches[0]
        count = self._positive_count(row["count"], metric, role)
        mean = self._finite_float(row["mean"], "mean", metric, role)
        stdev = self._finite_float(row["stdev"], "stdev", metric, role)
        return RoleSummary(role, count, mean, stdev)

    def _positive_count(self, raw: str, metric: MetricSpec, role: str) -> int:
        value = self._finite_float(raw, "count", metric, role)
        if not value.is_integer() or value < 1:
            raise PlottingError(
                f"Metric has invalid count for role {role}: {metric.csv_name}: {self.case_dir}"
            )
        return int(value)

    def _finite_float(self, raw: str, field: str, metric: MetricSpec, role: str) -> float:
        try:
            value = float(raw)
        except ValueError as error:
            raise PlottingError(
                f"Metric has non-numeric {field} for role {role}: {metric.csv_name}: {self.case_dir}"
            ) from error
        if not math.isfinite(value):
            raise PlottingError(
                f"Metric has non-finite {field} for role {role}: {metric.csv_name}: {self.case_dir}"
            )
        return value

    def _require_same_count(self, summaries: list[MetricSummary]) -> None:
        expected = summaries[0].count
        for summary in summaries:
            if summary.baseline.count != expected or summary.variant.count != expected:
                raise PlottingError(
                    f"Inconsistent run count in summary.csv for metric {summary.spec.csv_name}: {self.case_dir}"
                )


class MeanDifferencePlot:
    """PDF chart for mean relative differences in one reproducer case."""

    def __init__(self, case_dir: Path, summaries: list[MetricSummary], output: Path) -> None:
        self.case_dir = case_dir
        self.summaries = summaries
        self.output = output

    def save(self) -> None:
        values = [summary.relative_difference_percent for summary in self.summaries]
        labels = [summary.cv_label for summary in self.summaries]
        colors = ["#1f9a7a" if value < 0.0 else "#c95f4a" for value in values]

        figure, axis = plt.subplots(figsize=(11.5, 7.0))
        x_positions = list(range(len(self.summaries)))
        bars = axis.bar(x_positions, values, color=colors, width=0.62, edgecolor="none")

        axis.axhline(0.0, color="#4f6273", linewidth=1.5)
        axis.grid(axis="y", color="#e5eaef", linewidth=1.0)
        axis.set_axisbelow(True)
        axis.set_ylabel("Difference, %", fontsize=13)
        axis.set_xticks(x_positions)
        axis.set_xticklabels(labels, fontsize=11, fontweight="bold")
        axis.yaxis.set_major_formatter(FuncFormatter(lambda value, _: f"{value:.0f}%"))
        axis.tick_params(axis="x", length=0, pad=14)
        axis.tick_params(axis="y", labelsize=11)
        axis.margins(x=0.04)

        for side in ("top", "right", "left", "bottom"):
            axis.spines[side].set_visible(False)

        lower, upper, label_offset = self._limits(values)
        axis.set_ylim(lower, upper)
        for bar, value in zip(bars, values):
            y = value + label_offset if value >= 0.0 else value - label_offset
            vertical_alignment = "bottom" if value >= 0.0 else "top"
            axis.text(
                bar.get_x() + bar.get_width() / 2.0,
                y,
                f"{value:.1f}%",
                ha="center",
                va=vertical_alignment,
                fontsize=12,
                fontweight="bold",
                color="#1f2933",
            )

        count = self.summaries[0].count
        title = (
            f"{self.case_dir.name}\n"
            f"Metric difference between original and modified variant (N = {count})"
        )
        axis.set_title(title, fontsize=16, pad=20)
        figure.subplots_adjust(left=0.08, right=0.99, top=0.82, bottom=0.24)

        self.output.parent.mkdir(parents=True, exist_ok=True)
        figure.savefig(self.output, format="pdf", bbox_inches="tight")
        plt.close(figure)

    def _limits(self, values: list[float]) -> tuple[float, float, float]:
        minimum = min(values + [0.0])
        maximum = max(values + [0.0])
        span = maximum - minimum
        if span == 0.0:
            span = max(abs(maximum), 1.0)

        padding = span * 0.18
        lower = minimum - padding
        upper = maximum + padding

        if minimum >= 0.0:
            lower = -max(span * 0.15, 1.0)
        if maximum <= 0.0:
            upper = max(span * 0.15, 1.0)

        label_offset = max(span * 0.025, 0.25)
        return lower, upper, label_offset


METRICS = (
    MetricSpec("JMH score", "JMH primary score, us/op"),
    MetricSpec("Allocations", "Allocations, B/op"),
    MetricSpec("Instructions", "Instructions, #/op"),
    MetricSpec("Memory loads", "Memory loads, #/op"),
    MetricSpec("Memory stores", "Memory stores, #/op"),
)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Plot mean relative differences for one reproducer case.")
    parser.add_argument("case_dir", help="Path to one reproducer case directory.")
    parser.add_argument("--output", help="Output PDF path. Defaults to <case_dir>/mean_difference.pdf.")
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    case_dir = Path(args.case_dir)
    output = Path(args.output) if args.output else case_dir / "mean_difference.pdf"
    try:
        if MATPLOTLIB_IMPORT_ERROR is not None:
            raise PlottingError("Missing Python dependency: matplotlib")
        summaries = SummaryCsv(case_dir).metric_summaries()
        MeanDifferencePlot(case_dir, summaries, output).save()
        return 0
    except PlottingError as error:
        print(str(error), file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
