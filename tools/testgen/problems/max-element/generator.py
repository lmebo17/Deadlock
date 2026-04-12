#!/usr/bin/env python3
import random, sys, json

seed = int(sys.argv[1]) if len(sys.argv) > 1 else 42
random.seed(seed)

if seed == 0:
    nums = [3, 1, 4, 1, 5]
elif seed == 1:
    nums = [-10, -20, -5]
elif seed == 2:
    nums = [7, 7, 7, 7]
elif seed == 3:
    nums = [42]
elif seed == 4:
    nums = [1000000000, 1, 2, 3, 4]
elif seed == 5:
    nums = [1, 2, 3, 4, 1000000000]
elif seed == 6:
    nums = [-i for i in range(1, 1001)]
elif seed == 7:
    nums = list(range(1000))
elif seed == 8:
    nums = list(range(999, -1, -1))
elif seed == 9:
    nums = [1000000000] * 100000
else:
    n = random.randint(1, 100000)
    nums = [random.randint(-10**9, 10**9) for _ in range(n)]

print(json.dumps(nums))
