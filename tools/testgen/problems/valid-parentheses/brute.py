#!/usr/bin/env python3
import sys
input = sys.stdin.readline

s = input().strip()

# Same logic, different implementation — repeatedly remove matched pairs
# (Much slower but verifiably correct)
stack = []
match = {')': '(', ']': '[', '}': '{'}
valid = True
for c in s:
    if c in '([{':
        stack.append(c)
    else:
        if len(stack) == 0:
            valid = False
            break
        top = stack[len(stack) - 1]
        if top != match[c]:
            valid = False
            break
        stack = stack[:len(stack) - 1]  # deliberately slow pop

if len(stack) != 0:
    valid = False

print("YES" if valid else "NO")
