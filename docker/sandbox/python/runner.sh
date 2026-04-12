#!/bin/bash
set -e
timeout "${TIME_LIMIT_SEC:-5}" python3 -u /code/solution.py < /code/input.txt > /code/output.txt 2>/code/runtime_error.txt
exit $?
