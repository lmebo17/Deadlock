#!/usr/bin/env python3
import json, sys
from bisect import bisect_left

nums = json.loads(input())

# O(n log n) patience sorting
tails = []
for x in nums:
    pos = bisect_left(tails, x)
    if pos == len(tails):
        tails.append(x)
    else:
        tails[pos] = x

print(json.dumps(len(tails)))
