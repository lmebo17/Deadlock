#!/usr/bin/env python3
import sys
from collections import Counter
input = sys.stdin.readline

n = int(input())
a = list(map(int, input().split()))

cnt = Counter(a)
max_freq = max(cnt.values())
# Among elements with max frequency, pick the smallest
result = min(x for x in cnt if cnt[x] == max_freq)
print(result)
