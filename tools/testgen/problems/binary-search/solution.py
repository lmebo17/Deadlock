#!/usr/bin/env python3
import sys
from bisect import bisect_left
input = sys.stdin.readline

n, q = map(int, input().split())
a = list(map(int, input().split()))

out = []
for _ in range(q):
    x = int(input())
    idx = bisect_left(a, x)
    if idx < n and a[idx] == x:
        out.append(str(idx + 1))
    else:
        out.append("-1")

print("\n".join(out))
