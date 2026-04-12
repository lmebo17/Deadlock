#!/usr/bin/env python3
import sys
input = sys.stdin.readline

s = input().strip()
print("YES" if s == s[::-1] else "NO")
