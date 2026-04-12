#!/bin/bash
set -e
cd /tmp
cp /code/solution.cpp solution.cpp
if ! g++ -O2 -o solution solution.cpp 2>/code/compile_error.txt; then
    exit 2
fi
timeout "${TIME_LIMIT_SEC:-5}" ./solution < /code/input.txt > /code/output.txt 2>/code/runtime_error.txt
exit $?
