#!/usr/bin/env python3
import sys
input = sys.stdin.readline

n, t = map(int, input().split())
a = list(map(int, input().split()))

seen = {}
for i, x in enumerate(a):
    comp = t - x
    if comp in seen:
        print(seen[comp] + 1, i + 1)
        break
    seen[x] = i
