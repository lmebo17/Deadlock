#!/usr/bin/env python3
import random, sys

seed = int(sys.argv[1]) if len(sys.argv) > 1 else 42
random.seed(seed)

OPEN = "([{"
CLOSE = ")]}"
MATCH = {')': '(', ']': '[', '}': '{'}

def gen_valid(max_len):
    """Generate a valid bracket sequence."""
    s = []
    stack = []
    target_len = random.randint(2, max_len)
    if target_len % 2 == 1:
        target_len -= 1
    target_len = max(target_len, 2)

    while len(s) < target_len:
        remaining = target_len - len(s)
        if not stack:
            # Must open
            idx = random.randint(0, 2)
            s.append(OPEN[idx])
            stack.append(idx)
        elif remaining <= len(stack):
            # Must close
            idx = stack.pop()
            s.append(CLOSE[idx])
        else:
            if random.random() < 0.5:
                idx = random.randint(0, 2)
                s.append(OPEN[idx])
                stack.append(idx)
            else:
                idx = stack.pop()
                s.append(CLOSE[idx])

    while stack:
        idx = stack.pop()
        s.append(CLOSE[idx])

    return "".join(s)

if seed == 0:
    print("()[]{}")
elif seed == 1:
    print("(]")
elif seed == 2:
    print("{[()]}")
elif seed == 3:
    # Edge: single open bracket
    print("(")
elif seed == 4:
    # Edge: single close bracket
    print(")")
elif seed == 5:
    # Edge: empty-like — just "()"
    print("()")
elif seed == 6:
    # Edge: mismatched nesting
    print("([)]")
elif seed == 7:
    # Edge: deeply nested
    print("((((((()))))))")
elif seed == 8:
    # Edge: all opens
    print("((((")
elif seed == 9:
    # Edge: long valid
    print(gen_valid(100000))
else:
    if random.random() < 0.4:
        # Generate valid
        print(gen_valid(100000))
    else:
        # Generate random (likely invalid)
        n = random.randint(1, 100000)
        chars = "()[]{}";
        print("".join(random.choice(chars) for _ in range(n)))
