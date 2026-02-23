# Comparator

Comparator is a research tool for comparing JVM JIT behavior on semantically equivalent code variants. It runs JMH benchmarks, extracts JIT artifacts (for example native code size), and builds CSV reports with per-variant metrics and scalar dissimilarity scores.

## Metrics

Comparator currently tracks:

- `JMH primary score, us/op`
- `Allocations, B` (`gc.alloc.rate.norm`)
- `Instructions, #/op` (`instructions:u`, optional)
- `Memory loads, #/op` (`mem_inst_retired.all_loads:u`, optional)
- `Memory stores, #/op` (`mem_inst_retired.all_stores:u`, optional)
- `Native code size, B`

`Instructions`, `Memory loads`, and `Memory stores` are collected via JMH `LinuxPerfNormProfiler`, so they are available only on systems with Linux `perf` support.

- If `perf` is unavailable (or disabled), these three metrics are omitted.
- Relative-difference aggregation then uses only available metrics.

## API usage

### Run an analysis

```java
import comparator.Analysis;
import comparator.method.TargetMethod;
import java.nio.file.Path;

final Path classpath = Path.of("examples", "loop-computations");
new Analysis(new TargetMethod(classpath, "PlainForExample", "run"))
        .results()
        .print(System.out);
```

The classpath argument must point to a directory or JAR that contains compiled classes.

The `examples/` folders do not include `.class` files, so compile them before running the examples.

### Run comparisons and save CSV

```java
import comparator.Analysis;
import comparator.comparison.CsvComparison;
import comparator.comparison.CsvComparisons;
import comparator.method.Classpath;
import comparator.method.TargetMethod;
import java.nio.file.Path;

final Classpath classpath = new Classpath(Path.of("examples", "loop-computations"));

new CsvComparisons(
        new CsvComparison(
                new Analysis(new TargetMethod(classpath, "PlainForExample", "run")),
                new Analysis(new TargetMethod(classpath, "StreamBoxedExample", "run")),
                new Analysis(new TargetMethod(classpath, "PlainForIndexedExample", "run"))
        ),
        new CsvComparison(
                new Analysis(new TargetMethod(classpath, "PlainForExample", "run")),
                new Analysis(new TargetMethod(classpath, "PlainForReplaceAllExample", "run"))
        )
).saveAsCsv(Path.of("comparisons.csv"));
```

Example of `comparisons.csv` content in table form:

Comparison 1

| Target | JMH primary score, us/op | Allocations, B | Instructions, #/op | Memory loads, #/op | Memory stores, #/op | Native code size, B | JIT artifacts mean dissimilarity score | JIT artifacts max dissimilarity score |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | --- | --- |
| PlainForExample::run | 23257.22528255636 | 3.999806395979417E7 | 1.23E8 | 8.41E7 | 5.62E7 | 2080 | Original | Original |
| StreamBoxedExample::run | 31427.46728332885 | 7.199834751770295E7 | 1.74E8 | 1.12E8 | 7.73E7 | 3616 | 0.274801543186 | 0.571492137425 |
| PlainForIndexedExample::run | 23843.3428349999 | 3.999806482927127E7 | 1.25E8 | 8.56E7 | 5.81E7 | 1792 | 0.063402197511 | 0.148760330539 |

Comparison 2

| Target | JMH primary score, us/op | Allocations, B | Instructions, #/op | Memory loads, #/op | Memory stores, #/op | Native code size, B | JIT artifacts mean dissimilarity score | JIT artifacts max dissimilarity score |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | --- | --- |
| PlainForExample::run | 25020.38236669786 | 3.999806643324631E7 | 1.29E8 | 8.78E7 | 5.94E7 | 2080 | Original | Original |
| PlainForReplaceAllExample::run | 34609.25680818749 | 7.199801608483697E7 | 1.83E8 | 1.17E8 | 8.01E7 | 2192 | 0.285114108902 | 0.571414983127 |

## Comparison metrics

For two result vectors $M_1$ and $M_2$, the per-component **symmetric relative difference** is

$$
d_i \;=\; \frac{2\,\lvert m_{1,i} - m_{2,i} \rvert}{\lvert m_{1,i} \rvert + \lvert m_{2,i} \rvert + \varepsilon},
$$

where $\varepsilon > 0$ is a small constant that prevents the undefined case $m_{1,i} = m_{2,i} = 0$ (i.e., $0/0$).

Comparator reports the **mean dissimilarity score** as RMS (L2) over all $n$ components:

$$
D_{\mathrm{mean}} \;=\; \sqrt{\frac{1}{n}\sum_{i=1}^{n} d_i^2}.
$$

Comparator also reports the **max dissimilarity score**:

$$
D_{\mathrm{max}} \;=\; \max_{i=1..n} d_i.
$$

$D_{\mathrm{mean}} = 0$ and $D_{\mathrm{max}} = 0$ mean complete equality for all included metrics; larger values indicate stronger dissimilarity.


## Build

```
./gradlew build
```
