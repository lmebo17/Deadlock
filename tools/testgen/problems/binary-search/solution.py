#!/usr/bin/env python3
import json, sys
from bisect import bisect_left

nums = json.loads(input())
target = json.loads(input())

idx = bisect_left(nums, target)
if idx < len(nums) and nums[idx] == target:
    print(json.dumps(idx))
else:
    print(json.dumps(-1))
