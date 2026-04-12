#!/usr/bin/env python3
import json, sys

nums = json.loads(input())

unique = []
for i, x in enumerate(nums):
    if i == 0 or x != nums[i - 1]:
        unique.append(x)

print(json.dumps(unique))
