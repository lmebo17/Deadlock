#!/usr/bin/env python3
import sys
input = sys.stdin.readline

n = int(input())
a = list(map(int, input().split()))

unique = []
for i, x in enumerate(a):
    if i == 0 or x != a[i - 1]:
        unique.append(x)

print(len(unique))
print(" ".join(map(str, unique)))
