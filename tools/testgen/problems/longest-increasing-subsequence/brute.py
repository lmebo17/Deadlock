#!/usr/bin/env python3
import json, sys

nums = json.loads(input())

n = len(nums)
# O(n^2) DP
dp = [1] * n
for i in range(1, n):
    for j in range(i):
        if nums[j] < nums[i]:
            dp[i] = max(dp[i], dp[j] + 1)

print(json.dumps(max(dp)))
