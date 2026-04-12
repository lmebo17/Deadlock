#!/usr/bin/env python3
import sys
from collections import Counter
input = sys.stdin.readline

s = input().strip()
t = input().strip()

if Counter(s) == Counter(t):
    print("YES")
else:
    print("NO")
