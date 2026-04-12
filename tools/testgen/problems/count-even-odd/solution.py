#!/usr/bin/env python3
import sys
input = sys.stdin.readline

n = int(input())
a = list(map(int, input().split()))
even = sum(1 for x in a if x % 2 == 0)
odd = n - even
print(even, odd)
