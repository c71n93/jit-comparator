# Comparator

Comparator runs JMH benchmarks for a target method, parses JIT logs, and prints combined results.

## Run an analysis

```java
import comparator.Analysis;
import comparator.method.TargetMethod;
import java.nio.file.Path;

final Path classpath = Path.of("examples", "for-vs-stream");
new Analysis(new TargetMethod(classpath, "PlainForExample", "runFor"))
        .results()
        .print(System.out);
```

The classpath argument must point to a directory or JAR that contains compiled classes.

The `examples/` folders do not include `.class` files, so compile them before running the examples.

## Build

```
./gradlew build
```
