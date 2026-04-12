#!/usr/bin/env python3
import json, sys
from collections import deque

grid = json.loads(input())
n = len(grid)
m = len(grid[0]) if n > 0 else 0

count = 0
for i in range(n):
    for j in range(m):
        if grid[i][j] == 1:
            count += 1
            queue = deque([(i, j)])
            grid[i][j] = 0
            while queue:
                r, c = queue.popleft()
                for dr, dc in [(-1, 0), (1, 0), (0, -1), (0, 1)]:
                    nr, nc = r + dr, c + dc
                    if 0 <= nr < n and 0 <= nc < m and grid[nr][nc] == 1:
                        grid[nr][nc] = 0
                        queue.append((nr, nc))

print(json.dumps(count))
