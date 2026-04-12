#!/usr/bin/env python3
import random, sys, json

seed = int(sys.argv[1]) if len(sys.argv) > 1 else 42
random.seed(seed)

if seed == 0:
    nums = [1, 2, 3, 4, 5]
elif seed == 1:
    nums = [2, 4, 6, 8]
elif seed == 2:
    nums = [1, 3, 5]
elif seed == 3:
    nums = [0]
elif seed == 4:
    nums = [1]
elif seed == 5:
    nums = [-2, -3, -4, -5]
elif seed == 6:
    nums = [2 * i for i in range(1000)]
elif seed == 7:
    nums = [2 * i + 1 for i in range(1000)]
elif seed == 8:
    nums = [1000000000, 999999999]
elif seed == 9:
    nums = [0] * 100
else:
    n = random.randint(1, 100000)
    nums = [random.randint(-10**9, 10**9) for _ in range(n)]

print(json.dumps(nums))
