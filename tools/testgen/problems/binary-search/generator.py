#!/usr/bin/env python3
import random, sys, json

seed = int(sys.argv[1]) if len(sys.argv) > 1 else 42
random.seed(seed)

if seed == 0:
    nums = [1, 3, 5, 7, 9]
    target = 3
elif seed == 1:
    nums = [10, 20, 30, 40]
    target = 25
elif seed == 2:
    nums = [-5, 0, 5]
    target = 0
elif seed == 3:
    # Edge: single element, found
    nums = [42]
    target = 42
elif seed == 4:
    # Edge: single element, not found
    nums = [42]
    target = 43
elif seed == 5:
    # Edge: search for value less than min
    nums = [10, 20, 30, 40, 50]
    target = 5
elif seed == 6:
    # Edge: search for value greater than max
    nums = [10, 20, 30, 40, 50]
    target = 55
elif seed == 7:
    # Edge: large array, found at beginning
    n = 1000
    nums = sorted(random.sample(range(-10**6, 10**6), n))
    target = nums[0]
elif seed == 8:
    # Edge: large array, found at end
    n = 1000
    nums = sorted(random.sample(range(-10**6, 10**6), n))
    target = nums[-1]
elif seed == 9:
    # Edge: max constraints, found in middle
    n = 100000
    nums = sorted(random.sample(range(-10**9, 10**9), n))
    target = nums[n // 2]
else:
    n = random.randint(1, 100000)
    nums = sorted(random.sample(range(-10**9, 10**9), n))
    if random.random() < 0.5:
        target = random.choice(nums)
    else:
        target = random.randint(-10**9, 10**9)

print(json.dumps(nums))
print(json.dumps(target))
