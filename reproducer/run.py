#!/usr/bin/env python3
"""Curated JIT instability reproducer runner."""

from __future__ import annotations

import argparse
import csv
import json
import math
import platform
import re
import shutil
import statistics
import subprocess
import sys
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path


MANIFEST_HEADER = [
    "case_id",
    "role",
    "classpath",
    "class_name",
    "method_name",
    "jit_log",
    "jmh_result",
    "label",
]


@dataclass(frozen=True)
class CaseVariant:
    """Variant source for one reproducer case role."""

    role: str
    source: Path


@dataclass(frozen=True)
class Case:
    """Curated reproducer case."""

    case_id: str
    baseline_source: Path
    variants: tuple[CaseVariant, ...]
    class_name: str
    method_name: str = "run"

    def roles(self) -> list[str]:
        return ["baseline"] + [variant.role for variant in self.variants]


class ReproducerError(RuntimeError):
    """Fatal reproducer error."""


def main() -> int:
    args = parse_args()
    repo_root = Path(__file__).resolve().parents[1]
    cases_root = resolve_root(repo_root, args.cases_root)
    runs_root = resolve_root(repo_root, args.runs_root)
    try:
        require_positive_runs(args.runs)
        preflight_perf()
        cases = selected_cases(discover_cases(cases_root), include_prefixes(args.include_cases))
        session = Session(repo_root, cases_root, runs_root, args.runs, args.include_cases, cases, args.session_id)
        session.run()
        return 0
    except ReproducerError as error:
        print(str(error), file=sys.stderr)
        return 1


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Run curated JIT instability reproducer cases.")
    parser.add_argument("--runs", type=int, required=True)
    parser.add_argument("--include-cases")
    parser.add_argument("--session-id")
    parser.add_argument("--cases-root", default="reproducer/cases")
    parser.add_argument("--runs-root", default="reproducer/runs")
    return parser.parse_args()


def resolve_root(repo_root: Path, raw: str) -> Path:
    path = Path(raw)
    if path.is_absolute():
        return path
    return repo_root / path


def require_positive_runs(runs: int) -> None:
    if runs < 1:
        raise ReproducerError("--runs must be a positive integer")


def now() -> str:
    return datetime.now().astimezone().isoformat(timespec="seconds")


def session_id() -> str:
    return datetime.now().strftime("%Y%m%d_%H%M%S")


def log(message: str) -> None:
    print(f"[{now()}] {message}", flush=True)


def run_command(command: list[str], cwd: Path | None = None) -> subprocess.CompletedProcess[str]:
    return subprocess.run(command, cwd=cwd, capture_output=True, text=True, check=False)


def combined_output(process: subprocess.CompletedProcess[str]) -> str:
    return process.stdout + process.stderr


def command_path(name: str) -> str:
    resolved = shutil.which(name)
    if resolved is None:
        return name
    return resolved


def classpath_line(line: str) -> bool:
    return line.startswith("/") and "build/classes/java/main" in line


def preflight_perf() -> None:
    version = run_command(["perf", "--version"])
    if version.returncode != 0:
        raise ReproducerError(
            "perf is required but was not found in PATH.\nInstall Linux perf and rerun reproducer."
        )
    probe = run_command(["perf", "stat", "-e", "instructions", "--", "sleep", "0.1"])
    if probe.returncode != 0:
        raise ReproducerError(
            "perf is installed but cannot collect required events.\n"
            "Tried: perf stat -e instructions -- sleep 0.1\n"
            "This usually means perf_event_paranoid is too restrictive or the environment does not expose perf "
            "counters.\nFix Linux perf permissions, then rerun reproducer."
        )


def include_prefixes(raw: str | None) -> list[str]:
    if raw is None:
        return []
    prefixes = [part.strip() for part in raw.split(",")]
    if any(part == "" for part in prefixes):
        raise ReproducerError("--include-cases contains an empty item")
    return prefixes


def discover_cases(cases_root: Path) -> list[Case]:
    if not cases_root.is_dir():
        raise ReproducerError(f"Cases root does not exist: {cases_root}")
    cases = [case_from_directory(path) for path in sorted(cases_root.iterdir()) if path.is_dir()]
    if not cases:
        raise ReproducerError(f"No cases found in {cases_root}")
    return cases


def case_from_directory(path: Path) -> Case:
    baseline = single_java_file(path / "baseline", path.name, "baseline")
    variants = variant_sources(path, baseline.name)
    source_contract(baseline)
    for variant in variants:
        source_contract(variant.source)
    return Case(path.name, baseline, tuple(variants), baseline.stem)


def variant_sources(case_root: Path, baseline_name: str) -> list[CaseVariant]:
    variants: list[CaseVariant] = []
    legacy = case_root / "variant"
    if legacy.exists():
        variants.append(CaseVariant("variant", matching_java_file(legacy, case_root.name, "variant", baseline_name)))
    modern = case_root / "variants"
    if modern.exists():
        if not modern.is_dir():
            raise ReproducerError(f"Case {case_root.name} variants path is not a directory: {modern}")
        for role_dir in sorted(path for path in modern.iterdir() if path.is_dir()):
            role = role_dir.name
            require_role(role, case_root.name)
            variants.append(
                CaseVariant(role, matching_java_file(role_dir, case_root.name, role, baseline_name))
            )
    if not variants:
        raise ReproducerError(f"Case {case_root.name} must contain variant/ or variants/<role>/ directories")
    roles = [variant.role for variant in variants]
    if len(roles) != len(set(roles)):
        raise ReproducerError(f"Case {case_root.name} contains duplicate variant roles: {','.join(roles)}")
    return variants


def matching_java_file(directory: Path, case_id: str, role: str, baseline_name: str) -> Path:
    source = single_java_file(directory, case_id, role)
    if source.name != baseline_name:
        raise ReproducerError(
            f"Case {case_id} role {role} class file must match baseline file name: {baseline_name}"
        )
    return source


def require_role(role: str, case_id: str) -> None:
    if not re.fullmatch(r"[a-z][a-z0-9_]*", role):
        raise ReproducerError(f"Case {case_id} variant role must be a lowercase identifier: {role}")
    if role == "baseline":
        raise ReproducerError(f"Case {case_id} variant role is reserved: {role}")


def single_java_file(directory: Path, case_id: str, role: str) -> Path:
    if not directory.is_dir():
        raise ReproducerError(f"Case {case_id} is missing {role} directory: {directory}")
    files = sorted(directory.glob("*.java"))
    if len(files) != 1:
        raise ReproducerError(f"Case {case_id} {role} must contain exactly one .java file")
    return files[0]


def source_contract(source: Path) -> None:
    text = source.read_text(encoding="utf-8")
    if re.search(r"^\s*package\s+", text, flags=re.MULTILINE):
        raise ReproducerError(f"Source must not declare a package: {source}")
    class_pattern = r"\bclass\s+" + re.escape(source.stem) + r"\b"
    if re.search(class_pattern, text) is None:
        raise ReproducerError(f"Class name must match file name: {source}")
    if re.search(r"\bstatic\b[\s\S]*?\brun\s*\(\s*\)", text) is None:
        raise ReproducerError(f"Source must expose a no-argument static run method: {source}")


def selected_cases(cases: list[Case], prefixes: list[str]) -> list[Case]:
    if not prefixes:
        return sorted(cases, key=lambda case: case.case_id)
    selected: dict[str, Case] = {}
    for prefix in prefixes:
        matches = [case for case in cases if case.case_id.startswith(prefix)]
        if not matches:
            raise ReproducerError(f"--include-cases item matches no cases: {prefix}")
        for case in matches:
            selected[case.case_id] = case
    return [selected[key] for key in sorted(selected)]


class Session:
    """One reproducer execution session."""

    def __init__(
        self,
        repo_root: Path,
        cases_root: Path,
        runs_root: Path,
        runs: int,
        include_cases: str | None,
        cases: list[Case],
        requested_session_id: str | None,
    ) -> None:
        self.repo_root = repo_root
        self.cases_root = cases_root
        self.runs_root = runs_root
        self.runs = runs
        self.include_cases = include_prefixes(include_cases)
        self.cases = cases
        self.session_id = requested_session_id or session_id()
        self.root = runs_root / self.session_id
        self.index_rows: list[dict[str, str]] = []
        self.metadata = self.initial_metadata()

    def run(self) -> None:
        self.root.mkdir(parents=True, exist_ok=False)
        self.write_metadata()
        try:
            runtime_classpath = self.runtime_classpath()
            self.metadata["gradle"]["runtime_classpath"] = runtime_classpath
            self.write_case_metadata()
            for case in self.cases:
                self.run_case(case, runtime_classpath)
                aggregate_case(self.root, self.session_id, case)
            self.metadata["finished_at"] = now()
            self.write_index()
            self.write_metadata()
            self.update_latest()
        except ReproducerError:
            self.metadata["finished_at"] = now()
            self.write_index()
            self.write_metadata()
            raise

    def initial_metadata(self) -> dict:
        return {
            "session_id": self.session_id,
            "started_at": now(),
            "finished_at": "",
            "repo_root": str(self.repo_root),
            "cases_root": str(self.cases_root),
            "runs_root": str(self.runs_root),
            "runs": self.runs,
            "include_cases": self.include_cases,
            "selected_cases": [case.case_id for case in self.cases],
            "tools": tools_metadata(),
            "environment": environment_metadata(),
            "git": git_metadata(self.repo_root),
            "gradle": {
                "classpath_command": ["./gradlew", "classes", "printRuntimeClasspath"],
                "runtime_classpath": "",
                "command_output": "",
            },
        }

    def runtime_classpath(self) -> str:
        command = ["./gradlew", "classes", "printRuntimeClasspath"]
        process = run_command(command, self.repo_root)
        self.metadata["gradle"]["command_output"] = combined_output(process)
        self.write_metadata()
        if process.returncode != 0:
            raise ReproducerError("Gradle runtime classpath command failed:\n" + combined_output(process))
        lines = [line.strip() for line in process.stdout.splitlines() if classpath_line(line.strip())]
        if not lines:
            raise ReproducerError("Gradle runtime classpath command produced no classpath")
        return lines[-1]

    def write_case_metadata(self) -> None:
        for case in self.cases:
            case_root = self.root / "cases" / case.case_id
            case_root.mkdir(parents=True, exist_ok=True)
            write_json(
                case_root / "case_metadata.json",
                {
                    "case_id": case.case_id,
                    "baseline_source": str(case.baseline_source),
                    "variants": [
                        {"role": variant.role, "source": str(variant.source)}
                        for variant in case.variants
                    ],
                    "class_name": case.class_name,
                    "method_name": case.method_name,
                    "runs": self.runs,
                },
            )

    def run_case(self, case: Case, runtime_classpath: str) -> None:
        for index in range(1, self.runs + 1):
            run_name = f"run-{index:03d}"
            run_root = self.root / "cases" / case.case_id / "runs" / run_name
            try:
                self.run_once(case, index, run_name, run_root, runtime_classpath)
                self.index_rows.append(index_row(self.session_id, case, index, run_name, "success", run_root))
            except ReproducerError:
                self.index_rows.append(index_row(self.session_id, case, index, run_name, "failed", run_root))
                raise

    def run_once(self, case: Case, index: int, run_name: str, run_root: Path, runtime_classpath: str) -> None:
        started = now()
        run_root.mkdir(parents=True, exist_ok=True)
        paths = RunPaths(run_root)
        try:
            log(f"Starting {case.case_id} {run_name}")
            paths.create(case.roles())
            compile_source(
                case.baseline_source, paths.classes("baseline"), case.class_name, "compile-baseline", paths
            )
            for variant in case.variants:
                compile_source(
                    variant.source, paths.classes(variant.role), case.class_name, f"compile-{variant.role}", paths
                )
            write_manifest(case, paths)
            log(f"Starting comparison for {case.case_id} {run_name}")
            compare(case, paths, runtime_classpath, self.repo_root)
            log(f"Finished comparison for {case.case_id} {run_name}")
            write_json(paths.status, {"status": "success", "stage": "done", "started_at": started, "finished_at": now()})
        except RunFailure as failure:
            log(f"Failed {case.case_id} {run_name} at {failure.stage}")
            write_json(paths.status, failure.status(started))
            raise ReproducerError(f"Case {case.case_id} {run_name} failed at {failure.stage}") from failure

    def write_index(self) -> None:
        write_csv(
            self.root / "index.csv",
            ["session_id", "case_id", "run_index", "run_name", "status", "comparisons_csv"],
            self.index_rows,
        )

    def write_metadata(self) -> None:
        write_json(self.root / "metadata.json", self.metadata)

    def update_latest(self) -> None:
        latest = self.runs_root / "latest"
        try:
            if latest.is_symlink() or latest.exists():
                latest.unlink()
            latest.symlink_to(self.root, target_is_directory=True)
        except OSError as error:
            print(f"Warning: unable to update latest symlink: {error}", file=sys.stderr)


class RunFailure(Exception):
    """Failed run stage with captured command output."""

    def __init__(self, stage: str, command: list[str], process: subprocess.CompletedProcess[str]) -> None:
        super().__init__(stage)
        self.stage = stage
        self.command = command
        self.process = process

    def status(self, started: str) -> dict:
        return {
            "status": "failed",
            "stage": self.stage,
            "command": self.command,
            "exit_code": self.process.returncode,
            "stdout": self.process.stdout,
            "stderr": self.process.stderr,
            "started_at": started,
            "finished_at": now(),
        }


class RunPaths:
    """Filesystem paths for one case run."""

    def __init__(self, root: Path) -> None:
        self.root = root
        self.status = root / "status.json"
        self.logs = root / "logs"
        self.pairs = root / "pairs.csv"
        self.comparisons = root / "comparisons.csv"

    def classes(self, role: str) -> Path:
        return self.root / "classes" / role

    def artifacts(self, role: str) -> Path:
        return self.root / "artifacts" / role

    def create(self, roles: list[str]) -> None:
        for role in roles:
            self.classes(role).mkdir(parents=True, exist_ok=True)
            self.artifacts(role).mkdir(parents=True, exist_ok=True)
        for directory in [self.logs]:
            directory.mkdir(parents=True, exist_ok=True)


def compile_source(source: Path, classes: Path, class_name: str, stage: str, paths: RunPaths) -> None:
    command = ["javac", "-d", str(classes), str(source)]
    process = run_command(command)
    save_process_logs(process, paths.logs, stage)
    if process.returncode != 0:
        raise RunFailure(stage, command, process)
    expected = classes / f"{class_name}.class"
    if not expected.is_file():
        failed = subprocess.CompletedProcess(command, 1, "", f"Expected class file was not created: {expected}")
        raise RunFailure(stage, command, failed)


def write_manifest(case: Case, paths: RunPaths) -> None:
    rows = [manifest_row(case, "baseline", paths.classes("baseline"), paths.artifacts("baseline"))]
    for variant in case.variants:
        rows.append(manifest_row(case, variant.role, paths.classes(variant.role), paths.artifacts(variant.role)))
    write_csv(paths.pairs, MANIFEST_HEADER, rows)


def manifest_row(case: Case, role: str, classes: Path, artifacts: Path) -> dict[str, str]:
    return {
        "case_id": case.case_id,
        "role": role,
        "classpath": str(classes.resolve()),
        "class_name": case.class_name,
        "method_name": case.method_name,
        "jit_log": str((artifacts / "jit-log.xml").resolve()),
        "jmh_result": str((artifacts / "jmh-result.json").resolve()),
        "label": f"{case.case_id}/{role}",
    }


def compare(case: Case, paths: RunPaths, runtime_classpath: str, repo_root: Path) -> None:
    command = [
        "java",
        "-cp",
        runtime_classpath,
        "comparator.reproducer.ComparePairs",
        "--manifest",
        str(paths.pairs),
        "--output",
        str(paths.comparisons),
    ]
    process = run_command(command, repo_root)
    save_process_logs(process, paths.logs, "compare")
    if process.returncode != 0:
        raise RunFailure("compare", command, process)
    if not paths.comparisons.is_file():
        failed = subprocess.CompletedProcess(command, 1, "", f"Comparisons CSV was not created for {case.case_id}")
        raise RunFailure("compare", command, failed)


def save_process_logs(process: subprocess.CompletedProcess[str], logs: Path, stage: str) -> None:
    logs.mkdir(parents=True, exist_ok=True)
    (logs / f"{stage}.stdout.log").write_text(process.stdout, encoding="utf-8")
    (logs / f"{stage}.stderr.log").write_text(process.stderr, encoding="utf-8")


def aggregate_case(session_root: Path, sid: str, case: Case) -> None:
    case_root = session_root / "cases" / case.case_id
    rows: list[dict[str, str]] = []
    header: list[str] = []
    for run_dir in sorted((case_root / "runs").glob("run-*")):
        run_index = int(run_dir.name.removeprefix("run-"))
        with (run_dir / "comparisons.csv").open(newline="", encoding="utf-8") as stream:
            reader = csv.DictReader(stream)
            header = reader.fieldnames or header
            for row in reader:
                updated = dict(row)
                updated["session_id"] = sid
                updated["case_id"] = case.case_id
                updated["run_index"] = str(run_index)
                updated["run_name"] = run_dir.name
                updated["role"] = role_from_target(row.get("Target", ""))
                rows.append(updated)
    if not rows:
        raise ReproducerError(f"No comparison rows found for aggregation: {case.case_id}")
    all_header = header + ["session_id", "case_id", "run_index", "run_name", "role"]
    write_csv(case_root / "all_runs.csv", all_header, rows)
    write_csv(case_root / "summary.csv", ["role", "metric", "count", "mean", "stdev", "min", "max"], summary_rows(rows))


def role_from_target(target: str) -> str:
    if "/" in target:
        return target.rsplit("/", maxsplit=1)[1]
    return ""


def summary_rows(rows: list[dict[str, str]]) -> list[dict[str, str]]:
    grouped: dict[str, list[dict[str, str]]] = {}
    for row in rows:
        grouped.setdefault(row["role"], []).append(row)
    result: list[dict[str, str]] = []
    for role in sorted(grouped):
        columns = numeric_columns(grouped[role])
        for column in columns:
            values = [float(row[column]) for row in grouped[role] if is_number(row.get(column, ""))]
            result.append(summary_row(role, column, values))
    return result


def numeric_columns(rows: list[dict[str, str]]) -> list[str]:
    columns = rows[0].keys()
    return [column for column in columns if any(is_number(row.get(column, "")) for row in rows)]


def summary_row(role: str, metric: str, values: list[float]) -> dict[str, str]:
    return {
        "role": role,
        "metric": metric,
        "count": str(len(values)),
        "mean": str(statistics.fmean(values)),
        "stdev": str(statistics.stdev(values) if len(values) > 1 else 0.0),
        "min": str(min(values)),
        "max": str(max(values)),
    }


def is_number(value: str) -> bool:
    try:
        number = float(value)
        return math.isfinite(number)
    except ValueError:
        return False


def index_row(sid: str, case: Case, index: int, run_name: str, status: str, run_root: Path) -> dict[str, str]:
    return {
        "session_id": sid,
        "case_id": case.case_id,
        "run_index": str(index),
        "run_name": run_name,
        "status": status,
        "comparisons_csv": str(run_root / "comparisons.csv"),
    }


def tools_metadata() -> dict:
    return {
        "java": tool_metadata("java", ["java", "-version"]),
        "javac": tool_metadata("javac", ["javac", "-version"]),
        "perf": tool_metadata("perf", ["perf", "--version"]),
    }


def tool_metadata(name: str, command: list[str]) -> dict:
    process = run_command(command)
    return {"command": command_path(name), "version_output": combined_output(process)}


def environment_metadata() -> dict:
    return {
        "platform": platform.platform(),
        "machine": platform.machine(),
        "processor": platform.processor(),
        "python_version": platform.python_version(),
    }


def git_metadata(repo_root: Path) -> dict:
    commit = run_command(["git", "rev-parse", "HEAD"], repo_root)
    status = run_command(["git", "status", "--porcelain"], repo_root)
    return {"commit": commit.stdout.strip(), "dirty": bool(status.stdout.strip())}


def write_json(path: Path, value: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2, sort_keys=True) + "\n", encoding="utf-8")


def write_csv(path: Path, header: list[str], rows: list[dict[str, str]]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", newline="", encoding="utf-8") as stream:
        writer = csv.DictWriter(stream, fieldnames=header)
        writer.writeheader()
        writer.writerows(rows)


if __name__ == "__main__":
    raise SystemExit(main())
