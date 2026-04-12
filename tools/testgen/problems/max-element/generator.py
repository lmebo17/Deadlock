#!/usr/bin/env python3
import random, sys

seed = int(sys.argv[1]) if len(sys.argv) > 1 else 42
random.seed(seed)

if seed == 0:
    print(5)
    print("3 1 4 1 5")
elif seed == 1:
    print(3)
    print("-10 -20 -5")
elif seed == 2:
    print(4)
    print("7 7 7 7")
elif seed == 3:
    # Edge: single element
    print(1)
    print("42")
elif seed == 4:
    # Edge: max at beginning
    print(5)
    print("1000000000 1 2 3 4")
elif seed == 5:
    # Edge: max at end
    print(5)
    print("1 2 3 4 1000000000")
elif seed == 6:
    # Edge: all negative
    n = 1000
    print(n)
    print(" ".join(str(-i) for i in range(1, n + 1)))
elif seed == 7:
    # Edge: sorted ascending
    n = 1000
    print(n)
    print(" ".join(str(i) for i in range(n)))
elif seed == 8:
    # Edge: sorted descending
    n = 1000
    print(n)
    print(" ".join(str(i) for i in range(n - 1, -1, -1)))
elif seed == 9:
    # Edge: all same max
    n = 100000
    print(n)
    print(" ".join(["1000000000"] * n))
else:
    n = random.randint(1, 100000)
    print(n)
    print(" ".join(str(random.randint(-10**9, 10**9)) for _ in range(n)))
