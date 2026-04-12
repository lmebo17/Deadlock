#!/usr/bin/env python3
import random, sys

seed = int(sys.argv[1]) if len(sys.argv) > 1 else 42
random.seed(seed)

if seed == 0:
    print(5)
    print("1 2 3 4 5")
elif seed == 1:
    print(4)
    print("2 4 6 8")
elif seed == 2:
    print(3)
    print("1 3 5")
elif seed == 3:
    # Edge: single even
    print(1)
    print("0")
elif seed == 4:
    # Edge: single odd
    print(1)
    print("1")
elif seed == 5:
    # Edge: negative even and odd
    print(4)
    print("-2 -3 -4 -5")
elif seed == 6:
    # Edge: all even
    n = 1000
    print(n)
    print(" ".join(str(2 * i) for i in range(n)))
elif seed == 7:
    # Edge: all odd
    n = 1000
    print(n)
    print(" ".join(str(2 * i + 1) for i in range(n)))
elif seed == 8:
    # Edge: max values
    print(2)
    print("1000000000 999999999")
elif seed == 9:
    # Edge: zeros
    n = 100
    print(n)
    print(" ".join(["0"] * n))
else:
    n = random.randint(1, 100000)
    print(n)
    print(" ".join(str(random.randint(-10**9, 10**9)) for _ in range(n)))
