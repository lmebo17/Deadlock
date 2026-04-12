#!/usr/bin/env python3
import json, sys

nums = json.loads(input())
target = json.loads(input())

found = -1
for i in range(len(nums)):
    if nums[i] == target:
        found = i
        break

print(json.dumps(found))
