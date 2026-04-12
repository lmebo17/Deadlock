#!/usr/bin/env python3
import json, sys

nums = json.loads(input())
even = sum(1 for x in nums if x % 2 == 0)
odd = len(nums) - even
print(json.dumps([even, odd]))
