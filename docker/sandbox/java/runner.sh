#!/bin/bash
cd /tmp
cp /code/*.java . 2>/dev/null
cp /code/solution.java . 2>/dev/null

# Compile all Java files
if ! javac *.java 2>/code/results/compile_error.txt; then
    echo "COMPILE_ERROR" > /code/results/status.txt
    exit 0
fi

# Determine entry point
MAIN_CLASS="Solution"
if [ -f "Main.class" ]; then MAIN_CLASS="Main"; fi

for input_file in $(ls /code/tests/*-input.txt 2>/dev/null | sort); do
    test_name=$(basename "$input_file" | sed 's/-input.txt//')
    timeout "${TIME_LIMIT_SEC:-5}" java -Xmx"${MEMORY_LIMIT_MB:-256}m" $MAIN_CLASS \
        < "$input_file" \
        > "/code/results/${test_name}-output.txt" \
        2>/code/results/${test_name}-error.txt
    echo "$?" > "/code/results/${test_name}-exit.txt"
done

echo "DONE" > /code/results/status.txt
