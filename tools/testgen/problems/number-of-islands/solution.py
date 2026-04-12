#!/usr/bin/env python3
import sys
from collections import deque
input = sys.stdin.readline

n, m = map(int, input().split())
grid = []
for _ in range(n):
    grid.append(list(input().strip()))

count = 0
for i in range(n):
    for j in range(m):
        if grid[i][j] == '1':
            count += 1
            # BFS to mark connected land
            queue = deque([(i, j)])
            grid[i][j] = '0'
            while queue:
                r, c = queue.popleft()
                for dr, dc in [(-1, 0), (1, 0), (0, -1), (0, 1)]:
                    nr, nc = r + dr, c + dc
                    if 0 <= nr < n and 0 <= nc < m and grid[nr][nc] == '1':
                        grid[nr][nc] = '0'
                        queue.append((nr, nc))

print(count)
