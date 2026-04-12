#!/bin/bash
# Run each test case
for input_file in $(ls /code/tests/*-input.txt 2>/dev/null | sort); do
    test_name=$(basename "$input_file" | sed 's/-input.txt//')
    timeout "${TIME_LIMIT_SEC:-5}" python3 -u /code/solution.py \
        < "$input_file" \
        > "/code/results/${test_name}-output.txt" \
        2>/code/results/${test_name}-error.txt
    echo "$?" > "/code/results/${test_name}-exit.txt"
done

echo "DONE" > /code/results/status.txt
