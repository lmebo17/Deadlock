#!/usr/bin/env python3
import sys
input = sys.stdin.readline

n, m = map(int, input().split())
adj = [[] for _ in range(n + 1)]
indeg = [0] * (n + 1)

for _ in range(m):
    u, v = map(int, input().split())
    adj[u].append(v)
    indeg[v] += 1

# Brute Kahn's: scan all vertices each time to find minimum with indeg 0
result = []
removed = [False] * (n + 1)

for _ in range(n):
    # Find smallest vertex with in-degree 0
    chosen = -1
    for v in range(1, n + 1):
        if not removed[v] and indeg[v] == 0:
            chosen = v
            break
    removed[chosen] = True
    result.append(chosen)
    for v in adj[chosen]:
        indeg[v] -= 1

print(" ".join(map(str, result)))
