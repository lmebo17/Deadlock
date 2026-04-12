#!/usr/bin/env python3
import json, sys

a = json.loads(input())
b = json.loads(input())

result = []
i, j = 0, 0
while i < len(a) and j < len(b):
    if a[i] <= b[j]:
        result.append(a[i])
        i += 1
    else:
        result.append(b[j])
        j += 1
while i < len(a):
    result.append(a[i])
    i += 1
while j < len(b):
    result.append(b[j])
    j += 1

print(json.dumps(result))
