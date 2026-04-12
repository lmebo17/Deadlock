#!/usr/bin/env python3
import random, sys, json

seed = int(sys.argv[1]) if len(sys.argv) > 1 else 42
random.seed(seed)

if seed == 0:
    a = [1, 3, 5]
    b = [2, 4, 6]
elif seed == 1:
    a = [1, 2]
    b = [3, 4, 5]
elif seed == 2:
    a = [1, 1, 2, 2]
    b = [1, 2, 3]
elif seed == 3:
    a = [1]
    b = [2]
elif seed == 4:
    a = [1, 2, 3]
    b = [10, 20, 30]
elif seed == 5:
    a = [10, 20, 30]
    b = [1, 2, 3]
elif seed == 6:
    a = [1, 3, 5, 7, 9]
    b = [2, 4, 6, 8, 10]
elif seed == 7:
    a = [5, 5, 5, 5, 5]
    b = [5, 5, 5, 5, 5]
elif seed == 8:
    a = [-10, -5, -1]
    b = [-8, -3, 0]
elif seed == 9:
    n = 100000
    m = 100000
    a = sorted(random.randint(-10**9, 10**9) for _ in range(n))
    b = sorted(random.randint(-10**9, 10**9) for _ in range(m))
else:
    n = random.randint(1, 100000)
    m = random.randint(1, 100000)
    a = sorted(random.randint(-10**9, 10**9) for _ in range(n))
    b = sorted(random.randint(-10**9, 10**9) for _ in range(m))

print(json.dumps(a))
print(json.dumps(b))
