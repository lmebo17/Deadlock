#!/bin/bash
set -e
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
echo "Building Java sandbox..."
docker build -t deadlock-sandbox-java "$SCRIPT_DIR/java"
echo "Building Python sandbox..."
docker build -t deadlock-sandbox-python "$SCRIPT_DIR/python"
echo "Building C++ sandbox..."
docker build -t deadlock-sandbox-cpp "$SCRIPT_DIR/cpp"
echo "All sandbox images built successfully."
docker images | grep deadlock-sandbox
