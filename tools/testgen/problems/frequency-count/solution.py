#!/usr/bin/env python3
import json, sys
from collections import Counter

nums = json.loads(input())

cnt = Counter(nums)
max_freq = max(cnt.values())
result = min(x for x in cnt if cnt[x] == max_freq)
print(json.dumps(result))
