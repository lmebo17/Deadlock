#!/usr/bin/env python3
import random, sys, json

seed = int(sys.argv[1]) if len(sys.argv) > 1 else 42
random.seed(seed)

if seed == 0:
    nums = [1, 2, 3, 4, 5]
elif seed == 1:
    nums = [-1, -2, -3]
elif seed == 2:
    nums = [10, -5, 3, -8]
elif seed == 3:
    nums = [0]
elif seed == 4:
    nums = [1000000000]
elif seed == 5:
    nums = [-1000000000]
elif seed == 6:
    nums = [999999999] * 1000
elif seed == 7:
    nums = [0] * 100
elif seed == 8:
    nums = [10**9 if i % 2 == 0 else -(10**9) for i in range(100)]
elif seed == 9:
    nums = [1000000000] * 100000
else:
    n = random.randint(1, 100000)
    nums = [random.randint(-10**9, 10**9) for _ in range(n)]

print(json.dumps(nums))
