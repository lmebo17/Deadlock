#!/usr/bin/env python3
import random, sys

seed = int(sys.argv[1]) if len(sys.argv) > 1 else 42
random.seed(seed)

if seed == 0:
    print(6)
    print("1 2 2 3 3 3")
elif seed == 1:
    print(5)
    print("5 5 4 4 3")
elif seed == 2:
    print(4)
    print("1 2 3 4")
elif seed == 3:
    # Edge: single element
    print(1)
    print("42")
elif seed == 4:
    # Edge: two elements, tie -> smaller wins
    print(2)
    print("5 3")
elif seed == 5:
    # Edge: all same
    print(5)
    print("7 7 7 7 7")
elif seed == 6:
    # Edge: negative most frequent
    print(5)
    print("-3 -3 -3 1 2")
elif seed == 7:
    # Edge: tie between positive and negative -> negative wins
    print(4)
    print("-5 -5 3 3")
elif seed == 8:
    # Edge: large n, one dominant
    n = 100000
    arr = [1] * (n // 2) + [random.randint(2, 1000) for _ in range(n - n // 2)]
    random.shuffle(arr)
    print(n)
    print(" ".join(map(str, arr)))
elif seed == 9:
    # Edge: all unique, smallest element should win
    n = 100
    arr = list(range(1, n + 1))
    random.shuffle(arr)
    print(n)
    print(" ".join(map(str, arr)))
else:
    n = random.randint(1, 100000)
    # Use a smaller range to create duplicates
    val_range = max(1, n // 3)
    arr = [random.randint(-val_range, val_range) for _ in range(n)]
    print(n)
    print(" ".join(map(str, arr)))
