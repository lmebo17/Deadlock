#!/usr/bin/env python3
import json, sys

nums = json.loads(input())
print(json.dumps(sum(nums)))
