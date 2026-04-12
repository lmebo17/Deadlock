#!/usr/bin/env python3
import random, sys

seed = int(sys.argv[1]) if len(sys.argv) > 1 else 42
random.seed(seed)

if seed == 0:
    print(3)
    print("1 3 5")
    print(3)
    print("2 4 6")
elif seed == 1:
    print(2)
    print("1 2")
    print(3)
    print("3 4 5")
elif seed == 2:
    print(4)
    print("1 1 2 2")
    print(3)
    print("1 2 3")
elif seed == 3:
    # Edge: single elements
    print(1)
    print("1")
    print(1)
    print("2")
elif seed == 4:
    # Edge: first array all smaller
    print(3)
    print("1 2 3")
    print(3)
    print("10 20 30")
elif seed == 5:
    # Edge: second array all smaller
    print(3)
    print("10 20 30")
    print(3)
    print("1 2 3")
elif seed == 6:
    # Edge: interleaved
    print(5)
    print("1 3 5 7 9")
    print(5)
    print("2 4 6 8 10")
elif seed == 7:
    # Edge: all same values
    print(5)
    print("5 5 5 5 5")
    print(5)
    print("5 5 5 5 5")
elif seed == 8:
    # Edge: negatives
    print(3)
    print("-10 -5 -1")
    print(3)
    print("-8 -3 0")
elif seed == 9:
    # Edge: large arrays
    n = 100000
    m = 100000
    a = sorted(random.randint(-10**9, 10**9) for _ in range(n))
    b = sorted(random.randint(-10**9, 10**9) for _ in range(m))
    print(n)
    print(" ".join(map(str, a)))
    print(m)
    print(" ".join(map(str, b)))
else:
    n = random.randint(1, 100000)
    m = random.randint(1, 100000)
    a = sorted(random.randint(-10**9, 10**9) for _ in range(n))
    b = sorted(random.randint(-10**9, 10**9) for _ in range(m))
    print(n)
    print(" ".join(map(str, a)))
    print(m)
    print(" ".join(map(str, b)))
