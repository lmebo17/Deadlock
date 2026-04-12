#!/usr/bin/env python3
import random, sys

seed = int(sys.argv[1]) if len(sys.argv) > 1 else 42
random.seed(seed)

if seed == 0:
    print("4 5")
    print("11110")
    print("11010")
    print("11000")
    print("00000")
elif seed == 1:
    print("4 5")
    print("11000")
    print("11000")
    print("00100")
    print("00011")
elif seed == 2:
    print("3 3")
    print("101")
    print("010")
    print("101")
elif seed == 3:
    # Edge: single cell land
    print("1 1")
    print("1")
elif seed == 4:
    # Edge: single cell water
    print("1 1")
    print("0")
elif seed == 5:
    # Edge: all land
    print("3 3")
    print("111")
    print("111")
    print("111")
elif seed == 6:
    # Edge: all water
    print("3 3")
    print("000")
    print("000")
    print("000")
elif seed == 7:
    # Edge: checkerboard (max islands)
    n, m = 10, 10
    print(f"{n} {m}")
    for i in range(n):
        row = ""
        for j in range(m):
            row += "1" if (i + j) % 2 == 0 else "0"
        print(row)
elif seed == 8:
    # Edge: single row
    print("1 10")
    print("1011010110")
elif seed == 9:
    # Edge: single column
    print("10 1")
    for c in "1011010110":
        print(c)
else:
    n = random.randint(1, 500)
    m = random.randint(1, min(500, 250000 // max(n, 1)))
    density = random.uniform(0.2, 0.8)
    print(f"{n} {m}")
    for i in range(n):
        row = "".join("1" if random.random() < density else "0" for _ in range(m))
        print(row)
