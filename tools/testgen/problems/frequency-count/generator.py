#!/usr/bin/env python3
import random, sys, json

seed = int(sys.argv[1]) if len(sys.argv) > 1 else 42
random.seed(seed)

if seed == 0:
    nums = [1, 2, 2, 3, 3, 3]
elif seed == 1:
    nums = [5, 5, 4, 4, 3]
elif seed == 2:
    nums = [1, 2, 3, 4]
elif seed == 3:
    nums = [42]
elif seed == 4:
    nums = [5, 3]
elif seed == 5:
    nums = [7, 7, 7, 7, 7]
elif seed == 6:
    nums = [-3, -3, -3, 1, 2]
elif seed == 7:
    nums = [-5, -5, 3, 3]
elif seed == 8:
    n = 100000
    nums = [1] * (n // 2) + [random.randint(2, 1000) for _ in range(n - n // 2)]
    random.shuffle(nums)
elif seed == 9:
    n = 100
    nums = list(range(1, n + 1))
    random.shuffle(nums)
else:
    n = random.randint(1, 100000)
    val_range = max(1, n // 3)
    nums = [random.randint(-val_range, val_range) for _ in range(n)]

print(json.dumps(nums))
