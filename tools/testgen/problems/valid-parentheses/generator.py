#!/usr/bin/env python3
import random, sys, json

seed = int(sys.argv[1]) if len(sys.argv) > 1 else 42
random.seed(seed)

OPEN = "([{"
CLOSE = ")]}"

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
            idx = random.randint(0, 2)
            s.append(OPEN[idx])
            stack.append(idx)
        elif remaining <= len(stack):
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
    s = "()[]{}"
elif seed == 1:
    s = "(]"
elif seed == 2:
    s = "{[()]}"
elif seed == 3:
    s = "("
elif seed == 4:
    s = ")"
elif seed == 5:
    s = "()"
elif seed == 6:
    s = "([)]"
elif seed == 7:
    s = "((((((()))))))"
elif seed == 8:
    s = "(((("
elif seed == 9:
    s = gen_valid(100000)
else:
    if random.random() < 0.4:
        s = gen_valid(100000)
    else:
        n = random.randint(1, 100000)
        chars = "()[]{}"
        s = "".join(random.choice(chars) for _ in range(n))

print(json.dumps(s))
