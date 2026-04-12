#!/usr/bin/env python3
import sys
input = sys.stdin.readline

s = input().strip()
stack = []
match = {')': '(', ']': '[', '}': '{'}

valid = True
for c in s:
    if c in '([{':
        stack.append(c)
    elif c in ')]}':
        if not stack or stack[-1] != match[c]:
            valid = False
            break
        stack.pop()

if stack:
    valid = False

print("YES" if valid else "NO")
