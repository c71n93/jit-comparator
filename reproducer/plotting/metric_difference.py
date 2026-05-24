#!/usr/bin/env python3
"""Raw per-run metric charts for one reproducer case."""

from __future__ import annotations

import argparse
import csv
import math
import os
import statistics
import sys
import tempfile
import textwrap
from collections import Counter
from dataclasses import dataclass
from pathlib import Path
from typing import ClassVar

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
ROLE_LABELS = {"baseline": "orig", "variant": "ref"}


class PlottingError(RuntimeError):
    """Fatal raw metric plotting error."""


@dataclass(frozen=True)
class MetricSpec:
    """Fixed reproducer metric included in raw per-run charts."""

    label: str
    csv_name: str
    y_label: str
    output_name: str
    value_kind: str


@dataclass(frozen=True)
class RoleSeries:
    """Ordered raw values for one metric role."""

    role: str
    run_indices: tuple[int, ...]
    values: tuple[float, ...]

    @property
    def label(self) -> str:
        return ROLE_LABELS[self.role]

    @property
    def by_run_index(self) -> dict[int, float]:
        return dict(zip(self.run_indices, self.values))


@dataclass(frozen=True)
class MetricSeries:
    """Raw baseline and variant series for one reproducer metric."""

    spec: MetricSpec
    baseline: RoleSeries
    variant: RoleSeries

    @property
    def run_indices(self) -> tuple[int, ...]:
        return self.baseline.run_indices

    @property
    def roles(self) -> tuple[RoleSeries, RoleSeries]:
        return self.baseline, self.variant


@dataclass(frozen=True)
class MetricStatistics:
    """Calculated stability summary for one metric role."""

    spec: MetricSpec
    series: RoleSeries
    count: int
    mean: float
    stdev: float
    cv_percent: float
    value_range: float
    distinct_values_count: int
    frequency: tuple[tuple[float, int], ...]
    MAX_FULL_FREQUENCY_ITEMS: ClassVar[int] = 12

    @classmethod
    def from_series(
        cls,
        spec: MetricSpec,
        series: RoleSeries,
        case_dir: Path,
    ) -> MetricStatistics:
        values = list(series.values)
        mean = statistics.mean(values)
        if mean == 0.0:
            raise PlottingError(
                f"Cannot compute CV because mean is zero for metric {spec.csv_name} "
                f"role {series.role}: {case_dir}"
            )
        stdev = statistics.stdev(values) if len(values) > 1 else 0.0
        counts = Counter(values)
        return cls(
            spec=spec,
            series=series,
            count=len(values),
            mean=mean,
            stdev=stdev,
            cv_percent=stdev / mean * 100.0,
            value_range=max(values) - min(values),
            distinct_values_count=len(counts),
            frequency=tuple(sorted(counts.items(), key=lambda item: item[0])),
        )

    def panel_text(self) -> str:
        formatter = MetricValueFormatter(self.spec)
        return "\n".join(
            (
                self.series.label,
                f"CV: {self.cv_percent:.2f}%",
                f"Range: {formatter.with_unit(self.value_range)}",
                f"Distinct values: {self.distinct_values_count}",
                f"Mean: {formatter.with_unit(self.mean)}",
                f"Freq: {self._frequency_text(formatter)}",
            )
        )

    def _frequency_text(self, formatter: MetricValueFormatter) -> str:
        if len(self.frequency) > self.MAX_FULL_FREQUENCY_ITEMS:
            if all(count == 1 for _, count in self.frequency):
                first = formatter.compact(self.frequency[0][0])
                last = formatter.compact(self.frequency[-1][0])
                return f"{len(self.frequency)} unique values ({first}..{last})"
            return self._abbreviated_frequency_text(formatter)
        items = [
            f"{formatter.compact(value)}x{count}"
            for value, count in self.frequency
        ]
        return ", ".join(items)

    def _abbreviated_frequency_text(self, formatter: MetricValueFormatter) -> str:
        head_size = 6
        tail_size = 3
        head = self.frequency[:head_size]
        tail = self.frequency[-tail_size:]
        omitted = len(self.frequency) - head_size - tail_size
        head_text = [f"{formatter.compact(value)}x{count}" for value, count in head]
        tail_text = [f"{formatter.compact(value)}x{count}" for value, count in tail]
        return ", ".join([*head_text, f"... +{omitted} more", *tail_text])


class MetricValueFormatter:
    """Compact numeric formatter for one metric unit."""

    SI_UNITS = (
        (1_000_000_000.0, "G"),
        (1_000_000.0, "M"),
        (1_000.0, "K"),
    )

    def __init__(self, spec: MetricSpec) -> None:
        self.spec = spec

    def axis_tick(self, value: float) -> str:
        number, suffix = self._scaled(value)
        if self.spec.value_kind in ("bytes", "bytes_per_op"):
            return f"{number}{suffix}B"
        return f"{number}{suffix}"

    def with_unit(self, value: float) -> str:
        number, suffix = self._scaled(value)
        if self.spec.value_kind == "bytes":
            return f"{number} {suffix}B"
        if self.spec.value_kind == "bytes_per_op":
            return f"{number} {suffix}B/op"
        if self.spec.value_kind == "jmh":
            return f"{number} us/op"
        return f"{number} #/op"

    def compact(self, value: float) -> str:
        number, suffix = self._scaled(value)
        return f"{number}{suffix}"

    def _scaled(self, value: float) -> tuple[str, str]:
        absolute = abs(value)
        for threshold, suffix in self.SI_UNITS:
            if absolute >= threshold:
                return self._plain(value / threshold), suffix
        return self._plain(value), ""

    def _plain(self, value: float) -> str:
        if math.isclose(value, round(value), rel_tol=0.0, abs_tol=1e-9):
            return str(int(round(value)))
        formatted = f"{value:.4g}"
        if "e" in formatted or "E" in formatted:
            return formatted
        return formatted.rstrip("0").rstrip(".")


class AllRunsCsv:
    """Validated all_runs.csv source for one reproducer case."""

    def __init__(self, case_dir: Path) -> None:
        self.case_dir = case_dir
        self.path = case_dir / "all_runs.csv"

    def metric_series(self) -> list[MetricSeries]:
        self._require_case_directory()
        rows, fieldnames = self._rows()
        self._require_metric_columns(fieldnames)
        self._require_roles(rows)
        return [self._series_for(metric, rows) for metric in METRICS]

    def _require_case_directory(self) -> None:
        if not self.case_dir.is_dir():
            raise PlottingError(f"Case directory does not exist: {self.case_dir}")
        if not self.path.is_file():
            raise PlottingError(f"Missing all_runs.csv: {self.path}")

    def _rows(self) -> tuple[list[dict[str, str]], list[str]]:
        with self.path.open(newline="", encoding="utf-8") as source:
            reader = csv.DictReader(source)
            if reader.fieldnames is None:
                raise PlottingError(f"all_runs.csv is empty: {self.path}")
            missing = [name for name in REQUIRED_COLUMNS if name not in reader.fieldnames]
            if missing:
                raise PlottingError(
                    f"all_runs.csv is missing required columns {', '.join(missing)}: {self.path}"
                )
            return list(reader), list(reader.fieldnames)

    def _require_metric_columns(self, fieldnames: list[str]) -> None:
        for metric in METRICS:
            if metric.csv_name not in fieldnames:
                raise PlottingError(f"Missing metric column: {metric.csv_name}: {self.case_dir}")

    def _require_roles(self, rows: list[dict[str, str]]) -> None:
        roles = {row.get("role", "") for row in rows}
        for role in ROLES:
            if role not in roles:
                raise PlottingError(f"Missing role {role} in all_runs.csv: {self.case_dir}")

    def _series_for(self, metric: MetricSpec, rows: list[dict[str, str]]) -> MetricSeries:
        by_role = {role: self._role_series(metric, role, rows) for role in ROLES}
        baseline = by_role["baseline"]
        variant = by_role["variant"]
        self._require_matching_run_indices(metric, baseline, variant)
        return MetricSeries(metric, baseline, variant)

    def _role_series(
        self,
        metric: MetricSpec,
        role: str,
        rows: list[dict[str, str]],
    ) -> RoleSeries:
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
        run_indices = tuple(sorted(values))
        return RoleSeries(role, run_indices, tuple(values[index] for index in run_indices))

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
        baseline: RoleSeries,
        variant: RoleSeries,
    ) -> None:
        baseline_runs = set(baseline.run_indices)
        variant_runs = set(variant.run_indices)
        if baseline_runs != variant_runs:
            raise PlottingError(
                f"Run index mismatch for metric {metric.csv_name}: "
                f"baseline={sorted(baseline_runs)}, variant={sorted(variant_runs)}: {self.case_dir}"
            )


class MetricDifferencePlot:
    """PDF line chart for raw values of one reproducer metric."""

    COLORS = {"baseline": "#3f7ee8", "variant": "#dd7433"}
    MARKERS = {"baseline": "o", "variant": "s"}

    def __init__(self, case_dir: Path, metric: MetricSeries, output: Path) -> None:
        self.case_dir = case_dir
        self.metric = metric
        self.output = output

    def save(self) -> None:
        figure, axis = plt.subplots(figsize=(14.8, 7.6))
        formatter = MetricValueFormatter(self.metric.spec)
        statistics_by_role = [
            MetricStatistics.from_series(self.metric.spec, series, self.case_dir)
            for series in self.metric.roles
        ]

        for series, stats in zip(self.metric.roles, statistics_by_role):
            color = self.COLORS[series.role]
            axis.plot(
                series.run_indices,
                series.values,
                label=series.label,
                color=color,
                marker=self.MARKERS[series.role],
                linewidth=2.2,
                markersize=6.8,
                markeredgecolor="white",
                markeredgewidth=1.1,
            )
            axis.axhline(
                stats.mean,
                color=color,
                linestyle=(0, (4, 4)),
                linewidth=1.6,
                alpha=0.72,
            )

        axis.grid(axis="y", color="#e5eaef", linewidth=1.0)
        axis.grid(axis="x", color="#edf2f7", linewidth=0.8)
        axis.set_axisbelow(True)
        axis.set_xlabel("Run index", fontsize=13)
        axis.set_ylabel(self.metric.spec.y_label, fontsize=13)
        axis.yaxis.set_major_formatter(FuncFormatter(lambda value, _: formatter.axis_tick(value)))
        axis.xaxis.set_major_locator(MaxNLocator(integer=True))
        axis.tick_params(axis="both", labelsize=11, colors="#4f6273")
        axis.margins(x=0.02)

        for side in ("top", "right", "left", "bottom"):
            axis.spines[side].set_visible(False)

        axis.set_ylim(*self._limits())
        axis.legend(
            loc="upper left",
            bbox_to_anchor=(1.025, 1.0),
            frameon=False,
            fontsize=12,
            borderaxespad=0.0,
            handlelength=2.6,
        )
        axis.text(
            1.025,
            0.84,
            self._panel_text(statistics_by_role),
            transform=axis.transAxes,
            ha="left",
            va="top",
            fontsize=10.8,
            color="#34495e",
            linespacing=1.35,
            bbox={
                "boxstyle": "round,pad=1.0",
                "facecolor": "white",
                "edgecolor": "#dce3eb",
                "linewidth": 1.0,
            },
        )

        title = (
            f"{self.case_dir.name}\n"
            f"Metric \"{self.metric.spec.label}\" value for each experiment"
        )
        axis.set_title(title, fontsize=16, pad=18)
        figure.subplots_adjust(left=0.08, right=0.72, top=0.84, bottom=0.13)

        self.output.parent.mkdir(parents=True, exist_ok=True)
        figure.savefig(self.output, format="pdf", bbox_inches="tight")
        plt.close(figure)

    def _limits(self) -> tuple[float, float]:
        values = [value for series in self.metric.roles for value in series.values]
        minimum = min(values)
        maximum = max(values)
        span = maximum - minimum
        if span == 0.0:
            span = max(abs(maximum), 1.0)
        padding = span * 0.10
        return minimum - padding, maximum + padding

    def _panel_text(self, statistics_by_role: list[MetricStatistics]) -> str:
        sections = [stats.panel_text() for stats in statistics_by_role]
        wrapped = [self._wrap_frequency(section) for section in sections]
        return "\n\n".join(wrapped)

    def _wrap_frequency(self, section: str) -> str:
        lines = []
        for line in section.splitlines():
            if not line.startswith("Freq: "):
                lines.append(line)
                continue
            wrapped = textwrap.wrap(
                line,
                width=38,
                subsequent_indent="      ",
                break_long_words=False,
                break_on_hyphens=False,
            )
            lines.extend(wrapped)
        return "\n".join(lines)


METRICS = (
    MetricSpec(
        "JMH score",
        "JMH primary score, us/op",
        "JMH score, us/op",
        "jmh_score_by_run.pdf",
        "jmh",
    ),
    MetricSpec(
        "Allocations",
        "Allocations, B/op",
        "Allocations, B/op",
        "allocations_by_run.pdf",
        "bytes_per_op",
    ),
    MetricSpec(
        "Instructions",
        "Instructions, #/op",
        "Instructions, #/op",
        "instructions_by_run.pdf",
        "number_per_op",
    ),
    MetricSpec(
        "Memory loads",
        "Memory loads, #/op",
        "Memory loads, #/op",
        "memory_loads_by_run.pdf",
        "number_per_op",
    ),
    MetricSpec(
        "Memory stores",
        "Memory stores, #/op",
        "Memory stores, #/op",
        "memory_stores_by_run.pdf",
        "number_per_op",
    ),
    MetricSpec(
        "Native code size",
        "Native code size, B",
        "Native code size, B",
        "native_code_size_by_run.pdf",
        "bytes",
    ),
)


def load_metric_series(case_dir: Path) -> list[MetricSeries]:
    return AllRunsCsv(case_dir).metric_series()


def plot_metric(case_dir: Path, metric: MetricSeries, output: Path) -> None:
    MetricDifferencePlot(case_dir, metric, output).save()


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Plot raw per-run metric values for one reproducer case."
    )
    parser.add_argument("case_dir", help="Path to one reproducer case directory.")
    parser.add_argument(
        "--output-dir",
        help="Output directory. Defaults to <case_dir>/metric_difference/.",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    case_dir = Path(args.case_dir)
    output_dir = Path(args.output_dir) if args.output_dir else case_dir / "metric_difference"
    try:
        if MATPLOTLIB_IMPORT_ERROR is not None:
            raise PlottingError("Missing Python dependency: matplotlib")
        metrics = load_metric_series(case_dir)
        for metric in metrics:
            for series in metric.roles:
                MetricStatistics.from_series(metric.spec, series, case_dir)
        for metric in metrics:
            plot_metric(case_dir, metric, output_dir / metric.spec.output_name)
        return 0
    except PlottingError as error:
        print(str(error), file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
