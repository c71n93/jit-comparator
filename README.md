# Comparator

Comparator is a research tool for comparing JVM JIT behavior on semantically equivalent code variants. It runs JMH benchmarks, extracts JIT artifacts (for example native code size), and builds CSV reports with per-variant metrics and an equivalence flag.

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
import comparator.comparison.Comparison;
import comparator.comparison.Comparisons;
import comparator.method.Classpath;
import comparator.method.TargetMethod;
import java.nio.file.Path;

final Classpath classpath = new Classpath(Path.of("examples", "loop-computations"));

new Comparisons(
        new Comparison(
                new Analysis(new TargetMethod(classpath, "PlainForExample", "run")),
                new Analysis(new TargetMethod(classpath, "StreamBoxedExample", "run")),
                new Analysis(new TargetMethod(classpath, "PlainForIndexedExample", "run"))
        ),
        new Comparison(
                new Analysis(new TargetMethod(classpath, "PlainForExample", "run")),
                new Analysis(new TargetMethod(classpath, "PlainForReplaceAllExample", "run"))
        )
).saveAsCsv(Path.of("comparisons.csv"));
```

Example of `comparisons.csv` content in table form:

Comparison 1

| Target | JMH primary score, us/op | Allocations, B | Native code size, B | JIT artifacts equivalent? |
| --- | ---: | ---: | ---: | --- |
| PlainForExample::run | 23257.22528255636 | 3.999806395979417E7 | 2080 | Original |
| StreamBoxedExample::run | 31427.46728332885 | 7.199834751770295E7 | 3616 | false |
| PlainForIndexedExample::run | 23843.3428349999 | 3.999806482927127E7 | 1792 | false |

Comparison 2

| Target | JMH primary score, us/op | Allocations, B | Native code size, B | JIT artifacts equivalent? |
| --- | ---: | ---: | ---: | --- |
| PlainForExample::run | 25020.38236669786 | 3.999806643324631E7 | 2080 | Original |
| PlainForReplaceAllExample::run | 34609.25680818749 | 7.199801608483697E7 | 2192 | false |

## Build

```
./gradlew build
```
