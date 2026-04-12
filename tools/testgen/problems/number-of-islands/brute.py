#!/usr/bin/env python3
import sys
input = sys.stdin.readline

n, m = map(int, input().split())
grid = []
for _ in range(n):
    grid.append(list(input().strip()))

# Union-Find approach (different from BFS solution)
parent = list(range(n * m))

def find(x):
    while parent[x] != x:
        parent[x] = parent[parent[x]]
        x = parent[x]
    return x

def union(a, b):
    ra, rb = find(a), find(b)
    if ra != rb:
        parent[ra] = rb

for i in range(n):
    for j in range(m):
        if grid[i][j] == '1':
            if i + 1 < n and grid[i + 1][j] == '1':
                union(i * m + j, (i + 1) * m + j)
            if j + 1 < m and grid[i][j + 1] == '1':
                union(i * m + j, i * m + j + 1)

# Count unique roots of land cells
roots = set()
for i in range(n):
    for j in range(m):
        if grid[i][j] == '1':
            roots.add(find(i * m + j))

print(len(roots))
