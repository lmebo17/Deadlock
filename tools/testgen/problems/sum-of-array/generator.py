#!/usr/bin/env python3
import random, sys

seed = int(sys.argv[1]) if len(sys.argv) > 1 else 42
random.seed(seed)

if seed == 0:
    # Sample: small array
    print(5)
    print("1 2 3 4 5")
elif seed == 1:
    # Sample: negatives
    print(3)
    print("-1 -2 -3")
elif seed == 2:
    # Sample: mixed
    print(4)
    print("10 -5 3 -8")
elif seed == 3:
    # Edge: single element
    print(1)
    print("0")
elif seed == 4:
    # Edge: single element max
    print(1)
    print("1000000000")
elif seed == 5:
    # Edge: single element min
    print(1)
    print("-1000000000")
elif seed == 6:
    # Edge: all same
    n = 1000
    print(n)
    print(" ".join(["999999999"] * n))
elif seed == 7:
    # Edge: all zeros
    n = 100
    print(n)
    print(" ".join(["0"] * n))
elif seed == 8:
    # Edge: alternating max/-max (sum near zero)
    n = 100
    print(n)
    vals = []
    for i in range(n):
        vals.append(10**9 if i % 2 == 0 else -(10**9))
    print(" ".join(map(str, vals)))
elif seed == 9:
    # Edge: all max positive
    n = 100000
    print(n)
    print(" ".join(["1000000000"] * n))
else:
    # Random
    n = random.randint(1, 100000)
    print(n)
    print(" ".join(str(random.randint(-10**9, 10**9)) for _ in range(n)))
