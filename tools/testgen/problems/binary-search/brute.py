#!/usr/bin/env python3
import sys
input = sys.stdin.readline

n, q = map(int, input().split())
a = list(map(int, input().split()))

out = []
for _ in range(q):
    x = int(input())
    found = -1
    for i in range(n):
        if a[i] == x:
            found = i + 1
            break
    out.append(str(found))

print("\n".join(out))
