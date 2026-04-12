#!/bin/bash
set -e
cd /tmp
cp /code/solution.java Solution.java
if ! javac Solution.java 2>/code/compile_error.txt; then
    exit 2
fi
timeout "${TIME_LIMIT_SEC:-5}" java -Xmx"${MEMORY_LIMIT_MB:-256}m" Solution < /code/input.txt > /code/output.txt 2>/code/runtime_error.txt
exit $?
