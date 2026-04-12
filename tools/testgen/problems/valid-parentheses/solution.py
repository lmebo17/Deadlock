#!/usr/bin/env python3
import json, sys

s = json.loads(input())
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

print(json.dumps(valid))
