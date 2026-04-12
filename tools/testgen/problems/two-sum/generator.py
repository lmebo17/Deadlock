#!/usr/bin/env python3
import random, sys, json

seed = int(sys.argv[1]) if len(sys.argv) > 1 else 42
random.seed(seed)

if seed == 0:
    nums = [2, 7, 11, 15]
    target = 9
elif seed == 1:
    nums = [3, 2, 3]
    target = 6
elif seed == 2:
    nums = [1, -1, 2, 3, 4]
    target = 0
elif seed == 3:
    nums = [2, 3]
    target = 5
elif seed == 4:
    nums = [1000000000, -1000000000]
    target = 0
elif seed == 5:
    nums = [3, 1, 2, 4, 7]
    target = 10
elif seed == 6:
    nums = [-3, 1, -2, 4]
    target = -5
elif seed == 7:
    nums = [0, 5, 0]
    target = 0
elif seed == 8:
    n = 1000
    nums = list(range(1, n + 1))
    target = nums[n - 2] + nums[n - 1]
elif seed == 9:
    n = 1000
    nums = [500000000, 500000001] + list(range(2, n))
    target = 1000000001
else:
    n = random.randint(2, 100000)
    idx_i = random.randint(0, n - 2)
    idx_j = random.randint(idx_i + 1, n - 1)
    target = random.randint(-2 * 10**9, 2 * 10**9)
    a_val = random.randint(-10**9, 10**9)
    b_val = target - a_val
    if abs(b_val) > 10**9:
        a_val = target // 2
        b_val = target - a_val
    nums = []
    pair_set = {a_val, b_val}
    for k in range(n):
        if k == idx_i:
            nums.append(a_val)
        elif k == idx_j:
            nums.append(b_val)
        else:
            while True:
                v = random.randint(-10**9, 10**9)
                if target - v not in pair_set and v != a_val and v != b_val:
                    pair_set.add(v)
                    nums.append(v)
                    break

print(json.dumps(nums))
print(json.dumps(target))
