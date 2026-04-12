#!/usr/bin/env python3
import random, sys

seed = int(sys.argv[1]) if len(sys.argv) > 1 else 42
random.seed(seed)

if seed == 0:
    print(6)
    print("10 9 2 5 3 7")
elif seed == 1:
    print(8)
    print("0 1 0 3 2 3 4 5")
elif seed == 2:
    print(5)
    print("7 7 7 7 7")
elif seed == 3:
    # Edge: single element
    print(1)
    print("42")
elif seed == 4:
    # Edge: strictly increasing
    print(5)
    print("1 2 3 4 5")
elif seed == 5:
    # Edge: strictly decreasing
    print(5)
    print("5 4 3 2 1")
elif seed == 6:
    # Edge: two elements increasing
    print(2)
    print("1 2")
elif seed == 7:
    # Edge: two elements decreasing
    print(2)
    print("2 1")
elif seed == 8:
    # Edge: alternating
    print(8)
    print("1 5 2 6 3 7 4 8")
elif seed == 9:
    # Edge: large sorted
    n = 1000
    print(n)
    print(" ".join(str(i) for i in range(n)))
else:
    n = random.randint(1, 100000)
    print(n)
    print(" ".join(str(random.randint(-10**9, 10**9)) for _ in range(n)))
