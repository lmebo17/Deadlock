#!/usr/bin/env python3
import random, sys, json

seed = int(sys.argv[1]) if len(sys.argv) > 1 else 42
random.seed(seed)

if seed == 0:
    grid = [
        [1, 1, 1, 1, 0],
        [1, 1, 0, 1, 0],
        [1, 1, 0, 0, 0],
        [0, 0, 0, 0, 0]
    ]
elif seed == 1:
    grid = [
        [1, 1, 0, 0, 0],
        [1, 1, 0, 0, 0],
        [0, 0, 1, 0, 0],
        [0, 0, 0, 1, 1]
    ]
elif seed == 2:
    grid = [
        [1, 0, 1],
        [0, 1, 0],
        [1, 0, 1]
    ]
elif seed == 3:
    grid = [[1]]
elif seed == 4:
    grid = [[0]]
elif seed == 5:
    grid = [[1, 1, 1], [1, 1, 1], [1, 1, 1]]
elif seed == 6:
    grid = [[0, 0, 0], [0, 0, 0], [0, 0, 0]]
elif seed == 7:
    # Checkerboard
    n, m = 10, 10
    grid = []
    for i in range(n):
        row = []
        for j in range(m):
            row.append(1 if (i + j) % 2 == 0 else 0)
        grid.append(row)
elif seed == 8:
    # Single row
    grid = [[1, 0, 1, 1, 0, 1, 0, 1, 1, 0]]
elif seed == 9:
    # Single column
    grid = [[1], [0], [1], [1], [0], [1], [0], [1], [1], [0]]
else:
    n = random.randint(1, 500)
    m = random.randint(1, min(500, 250000 // max(n, 1)))
    density = random.uniform(0.2, 0.8)
    grid = []
    for i in range(n):
        row = [1 if random.random() < density else 0 for _ in range(m)]
        grid.append(row)

print(json.dumps(grid))
