# Comparator

Comparator is a research tool for comparing JVM JIT behavior on semantically equivalent code variants. It runs JMH benchmarks, extracts JIT artifacts (for example native code size), and builds CSV reports with per-variant metrics, metric-error columns, and scalar dissimilarity scores.

## Metrics

Comparator currently tracks:

- `JMH primary score, us/op`
- `JMH primary score relative error, ratio`
- `Allocations, B/op` (`gc.alloc.rate.norm`)
- `Allocations relative error, ratio`
- `Instructions, #/op` (`instructions:u`, optional)
- `Memory loads, #/op` (`mem_inst_retired.all_loads:u` on Intel, `ls_dispatch.ld_dispatch:u` on AMD, optional)
- `Memory stores, #/op` (`mem_inst_retired.all_stores:u` on Intel, `ls_dispatch.store_dispatch:u` on AMD, optional)
- `Native code size, B`

`Instructions`, `Memory loads`, and `Memory stores` are collected via JMH `LinuxPerfNormProfiler`, so they are available only on systems with Linux `perf` support. Comparator selects Intel or AMD load/store perf events automatically and aggregates split hybrid-CPU instruction counters such as `cpu_core/instructions` and `cpu_atom/instructions`.

CSV rows also include `JIT log file` and `JMH result file` path columns.

- The two `...relative error, ratio` columns are derived from JMH `scoreError / score`.
- These columns are reporting-only metric errors. They are written to CSV, but they are not used in mean/max dissimilarity calculations.
- If JMH cannot estimate `scoreError` for a metric, the corresponding relative-error value may be `NaN`.
- If `perf` is unavailable (or disabled), these three metrics are omitted.
- Relative-difference aggregation then uses only available comparable metrics.

## Target method limitations

- The target method should be `static`.
- The target method should not accept arguments.
- The target method should preferably return a value. `void` methods are more likely to be removed by JIT optimization as dead code.
- The target method should preferably execute at least `1000` instructions. Around `50-100` instructions may come from non-optimized JMH wrapper overhead (`Method.invoke()`). By default this optimization should work and overhead will be around `10-20` instructions.

## Reproducer

The `reproducer/` directory contains a runner for curated baseline/variant cases.
It compiles each case, runs Comparator repeatedly, preserves raw JIT/JMH artifacts,
and writes per-case aggregate CSV files.

See [reproducer/README.md](reproducer/README.md) for usage.

## API usage

### Run an analysis

```java
import comparator.Analysis;
import comparator.method.TargetMethod;
import java.nio.file.Path;

final Path classpath = Path.of(
        "reproducer", "cases", "case00_primitive_loop_examples", "baseline"
);
new Analysis(new TargetMethod(classpath, "PrimitiveLoopExample", "run"))
        .results()
        .print(System.out);
```

The classpath argument must point to a directory or JAR that contains compiled classes.

The `reproducer/cases/` folders do not include `.class` files, so compile the selected
case role before running API examples. For `case00_primitive_loop_examples`, each role
has its own classpath because all variants use the same `PrimitiveLoopExample` class name.

### Run an analysis with label

```java
import comparator.Analysis;
import comparator.method.TargetMethod;
import java.nio.file.Path;

final Path classpath = Path.of(
        "reproducer", "cases", "case00_primitive_loop_examples", "baseline"
);
new Analysis(
        new TargetMethod(classpath, "PrimitiveLoopExample", "run"),
        "baseline-for-loop"
).results().print(System.out);
```

The label is used as the `Target` value in CSV output.

### Run comparisons and save CSV

```java
import comparator.Analysis;
import comparator.comparison.CsvComparison;
import comparator.comparison.CsvComparisons;
import comparator.method.Classpath;
import comparator.method.TargetMethod;
import java.nio.file.Path;

final Path caseRoot = Path.of("reproducer", "cases", "case00_primitive_loop_examples");
final Classpath baseline = new Classpath(caseRoot.resolve("baseline"));
final Classpath plainArray = new Classpath(caseRoot.resolve("variants").resolve("plain_array"));
final Classpath indexedLoop = new Classpath(caseRoot.resolve("variants").resolve("indexed_loop"));
final Classpath streamBoxed = new Classpath(caseRoot.resolve("variants").resolve("stream_boxed"));

new CsvComparisons(
        new CsvComparison(
                new Analysis(new TargetMethod(baseline, "PrimitiveLoopExample", "run"), "baseline"),
                new Analysis(new TargetMethod(plainArray, "PrimitiveLoopExample", "run"), "plain_array"),
                new Analysis(new TargetMethod(indexedLoop, "PrimitiveLoopExample", "run"), "indexed_loop"),
                new Analysis(new TargetMethod(streamBoxed, "PrimitiveLoopExample", "run"), "stream_boxed")
        )
).saveAsCsv(Path.of("comparisons.csv"));
```

`CsvComparison` enables JIT metric comparison columns by default. To omit them, use `new CsvComparison(false, original, refactoring1, refactoring2, ...)`.

Example of `comparisons.csv` content in table form. File path columns are shortened for readability:

| Target | JMH primary score, us/op | JMH primary score relative error, ratio | Allocations, B/op | Allocations relative error, ratio | Instructions, #/op | Memory loads, #/op | Memory stores, #/op | Native code size, B | JIT log file | JMH result file | JIT metrics mean dissimilarity score | JIT metrics max dissimilarity score |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | --- | --- | --- | --- |
| baseline | 22.09 | 0.10 | 38016.15 | 0.00 | 149402.64 | 43000.08 | 20805.29 | 2256.00 | `.../PrimitiveLoopExample-jit-log-...xml` | `.../PrimitiveLoopExample-jmh-result-...json` | Original | Original |
| plain_array | 4.55 | 0.11 | 8040.03 | 0.00 | 25719.34 | 3281.44 | 1151.00 | 1552.00 | `.../PrimitiveLoopExample-jit-log-...xml` | `.../PrimitiveLoopExample-jmh-result-...json` | 1.40 | 1.79 |
| indexed_loop | 25.64 | 0.12 | 38016.18 | 0.00 | 149624.24 | 41981.45 | 19787.82 | 1960.00 | `.../PrimitiveLoopExample-jit-log-...xml` | `.../PrimitiveLoopExample-jmh-result-...json` | 0.09 | 0.15 |
| stream_boxed | 46.05 | 0.10 | 70232.32 | 0.00 | 243005.89 | 74453.67 | 32071.86 | 3528.00 | `.../PrimitiveLoopExample-jit-log-...xml` | `.../PrimitiveLoopExample-jmh-result-...json` | 0.54 | 0.70 |

### Labeled comparison example

```java
new CsvComparisons(
        new CsvComparison(
                new Analysis(new TargetMethod(baseline, "PrimitiveLoopExample", "run"), "Baseline"),
                new Analysis(new TargetMethod(streamBoxed, "PrimitiveLoopExample", "run"), "Stream")
        )
).saveAsCsv(Path.of("labels-demo.csv"));
```

Example of `labels-demo.csv` content in table form:

| Target | JMH primary score, us/op | JMH primary score relative error, ratio | Allocations, B/op | Allocations relative error, ratio | Instructions, #/op | Memory loads, #/op | Memory stores, #/op | Native code size, B | JIT log file | JMH result file | JIT metrics mean dissimilarity score | JIT metrics max dissimilarity score |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | --- | --- | --- | --- |
| Baseline | 22.09 | 0.10 | 38016.15 | 0.00 | 149402.64 | 43000.08 | 20805.29 | 2256.00 | `.../PrimitiveLoopExample-jit-log-...xml` | `.../PrimitiveLoopExample-jmh-result-...json` | Original | Original |
| Stream | 46.05 | 0.10 | 70232.32 | 0.00 | 243005.89 | 74453.67 | 32071.86 | 3528.00 | `.../PrimitiveLoopExample-jit-log-...xml` | `.../PrimitiveLoopExample-jmh-result-...json` | 0.54 | 0.70 |

## Comparison metrics

For two result vectors $M_1$ and $M_2$, the per-component **symmetric relative difference** is

$$
d_i = \frac{2\,\lvert m_{1,i} - m_{2,i} \rvert}{\lvert m_{1,i} \rvert + \lvert m_{2,i} \rvert + \varepsilon},
$$

where $\varepsilon > 0$ is a small constant that prevents the undefined case $m_{1,i} = m_{2,i} = 0$ (i.e., $0/0$).

Only comparable `Metric` values participate in this calculation. CSV-only `MetricError` columns such as `JMH primary score relative error, ratio` and `Allocations relative error, ratio` are excluded.

Comparator reports the **mean dissimilarity score** as RMS (L2) over all $n$ components:

$$
D_{\mathrm{mean}} = \sqrt{\frac{1}{n}\sum_{i=1}^{n} d_i^2}.
$$

Comparator also reports the **max dissimilarity score**:

$$
D_{\mathrm{max}} = \max_{i=1..n} d_i.
$$

$D_{\mathrm{mean}} = 0$ and $D_{\mathrm{max}} = 0$ mean complete equality for all included metrics; larger values indicate stronger dissimilarity.


## How to Contribute

**Before submitting changes, make sure the project passes verification:**

```bash
./gradlew check
```

**If you prefer to run the steps separately, use:**

```bash
./gradlew test
./gradlew unifycodeCheck
```

`test` runs the JUnit test suite. `unifycodeCheck` runs static verification without tests, including Spotless, PMD, and Checkstyle.

**Format the code with:**

```bash
./gradlew format
```

**Build the project with:**

```bash
./gradlew build
```
