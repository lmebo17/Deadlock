#!/usr/bin/env python3
import json, sys

nums = json.loads(input())
target = json.loads(input())

seen = {}
for i, x in enumerate(nums):
    comp = target - x
    if comp in seen:
        print(json.dumps([seen[comp], i]))
        sys.exit()
    seen[x] = i
