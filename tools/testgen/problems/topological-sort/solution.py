#!/usr/bin/env python3
import sys
import heapq
input = sys.stdin.readline

n, m = map(int, input().split())
adj = [[] for _ in range(n + 1)]
indeg = [0] * (n + 1)

for _ in range(m):
    u, v = map(int, input().split())
    adj[u].append(v)
    indeg[v] += 1

# Kahn's algorithm with min-heap for lexicographic smallest
heap = []
for i in range(1, n + 1):
    if indeg[i] == 0:
        heapq.heappush(heap, i)

result = []
while heap:
    u = heapq.heappop(heap)
    result.append(u)
    for v in adj[u]:
        indeg[v] -= 1
        if indeg[v] == 0:
            heapq.heappush(heap, v)

print(" ".join(map(str, result)))
