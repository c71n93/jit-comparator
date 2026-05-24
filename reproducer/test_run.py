"""Contract tests for the reproducer Python runner."""

from __future__ import annotations

import csv
import subprocess
import tempfile
import unittest
from pathlib import Path

import run


class DiscoveryTest(unittest.TestCase):
    """Case discovery contract tests."""

    def test_discovers_one_valid_case(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.write_case(root / "case01_example")
            cases = run.discover_cases(root)
            self.assertEqual(["case01_example"], [case.case_id for case in cases])
            self.assertEqual(["variant"], [variant.role for variant in cases[0].variants])

    def test_discovers_multi_variant_case(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            case_root = root / "case00_example"
            source = case_root / "baseline" / "Example.java"
            source.parent.mkdir(parents=True)
            source.write_text("class Example { public static int run() { return 1; } }", encoding="utf-8")
            for role in ["plain_array", "stream_boxed"]:
                variant = case_root / "variants" / role / "Example.java"
                variant.parent.mkdir(parents=True)
                variant.write_text("class Example { public static int run() { return 1; } }", encoding="utf-8")
            cases = run.discover_cases(root)
            self.assertEqual(["plain_array", "stream_boxed"], [variant.role for variant in cases[0].variants])

    def test_rejects_multiple_baseline_sources(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.write_case(root / "case01_example")
            (root / "case01_example" / "baseline" / "Other.java").write_text(
                "class Other { public static int run() { return 1; } }",
                encoding="utf-8",
            )
            with self.assertRaises(run.ReproducerError):
                run.discover_cases(root)

    def write_case(self, case_root: Path) -> None:
        for role in ["baseline", "variant"]:
            source = case_root / role / "Example.java"
            source.parent.mkdir(parents=True)
            source.write_text("class Example { public static int run() { return 1; } }", encoding="utf-8")


class AggregationTest(unittest.TestCase):
    """CSV aggregation contract tests."""

    def test_aggregates_synthetic_comparisons(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            case = run.Case(
                "case01_example",
                root / "baseline.java",
                (
                    run.CaseVariant("variant", root / "variant.java"),
                    run.CaseVariant("stream_boxed", root / "stream_boxed.java"),
                ),
                "Example",
            )
            run_dir = root / "cases" / case.case_id / "runs" / "run-001"
            run_dir.mkdir(parents=True)
            self.write_comparisons(run_dir / "comparisons.csv")
            run.aggregate_case(root, "session01", case)
            all_rows = self.read_csv(root / "cases" / case.case_id / "all_runs.csv")
            summary_rows = self.read_csv(root / "cases" / case.case_id / "summary.csv")
            self.assertEqual(["baseline", "variant", "stream_boxed"], [row["role"] for row in all_rows])
            self.assertTrue(any(row["metric"] == "score" for row in summary_rows))

    def test_writes_process_logs(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            logs = Path(directory) / "logs"
            process = subprocess.CompletedProcess(["example"], 0, "out", "err")
            run.save_process_logs(process, logs, "compare")
            self.assertEqual("out", (logs / "compare.stdout.log").read_text(encoding="utf-8"))
            self.assertEqual("err", (logs / "compare.stderr.log").read_text(encoding="utf-8"))

    def write_comparisons(self, path: Path) -> None:
        with path.open("w", newline="", encoding="utf-8") as stream:
            writer = csv.DictWriter(stream, fieldnames=["Target", "score"])
            writer.writeheader()
            writer.writerow({"Target": "case01_example/baseline", "score": "1.0"})
            writer.writerow({"Target": "case01_example/variant", "score": "2.0"})
            writer.writerow({"Target": "case01_example/stream_boxed", "score": "3.0"})

    def read_csv(self, path: Path) -> list[dict[str, str]]:
        with path.open(newline="", encoding="utf-8") as stream:
            return list(csv.DictReader(stream))


if __name__ == "__main__":
    unittest.main()
