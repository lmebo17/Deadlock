#!/usr/bin/env python3
import random, sys, json

seed = int(sys.argv[1]) if len(sys.argv) > 1 else 42
random.seed(seed)

if seed == 0:
    nums = [10, 9, 2, 5, 3, 7]
elif seed == 1:
    nums = [0, 1, 0, 3, 2, 3, 4, 5]
elif seed == 2:
    nums = [7, 7, 7, 7, 7]
elif seed == 3:
    nums = [42]
elif seed == 4:
    nums = [1, 2, 3, 4, 5]
elif seed == 5:
    nums = [5, 4, 3, 2, 1]
elif seed == 6:
    nums = [1, 2]
elif seed == 7:
    nums = [2, 1]
elif seed == 8:
    nums = [1, 5, 2, 6, 3, 7, 4, 8]
elif seed == 9:
    nums = list(range(1000))
else:
    n = random.randint(1, 100000)
    nums = [random.randint(-10**9, 10**9) for _ in range(n)]

print(json.dumps(nums))
