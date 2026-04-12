#!/usr/bin/env python3
import sys
input = sys.stdin.readline

n = int(input())
a = list(map(int, input().split()))
print(" ".join(map(str, reversed(a))))
