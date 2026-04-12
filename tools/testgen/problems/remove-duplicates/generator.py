#!/usr/bin/env python3
import random, sys

seed = int(sys.argv[1]) if len(sys.argv) > 1 else 42
random.seed(seed)

if seed == 0:
    print(6)
    print("1 1 2 3 3 4")
elif seed == 1:
    print(5)
    print("1 2 3 4 5")
elif seed == 2:
    print(4)
    print("7 7 7 7")
elif seed == 3:
    # Edge: single element
    print(1)
    print("42")
elif seed == 4:
    # Edge: two same
    print(2)
    print("5 5")
elif seed == 5:
    # Edge: two different
    print(2)
    print("3 7")
elif seed == 6:
    # Edge: all same large n
    n = 100000
    print(n)
    print(" ".join(["0"] * n))
elif seed == 7:
    # Edge: all unique
    n = 1000
    print(n)
    print(" ".join(str(i) for i in range(n)))
elif seed == 8:
    # Edge: negative values with duplicates
    print(7)
    print("-5 -5 -3 -1 -1 0 0")
elif seed == 9:
    # Edge: max range
    print(5)
    print("-1000000000 -1000000000 0 1000000000 1000000000")
else:
    n = random.randint(1, 100000)
    arr = sorted(random.randint(-10**9, 10**9) for _ in range(n))
    print(n)
    print(" ".join(map(str, arr)))
