#!/usr/bin/env python3
import random, sys

seed = int(sys.argv[1]) if len(sys.argv) > 1 else 42
random.seed(seed)

if seed == 0:
    print("5 3")
    print("1 3 5 7 9")
    print("3")
    print("6")
    print("9")
elif seed == 1:
    print("4 2")
    print("10 20 30 40")
    print("10")
    print("25")
elif seed == 2:
    print("3 3")
    print("-5 0 5")
    print("-5")
    print("0")
    print("5")
elif seed == 3:
    # Edge: single element, found
    print("1 1")
    print("42")
    print("42")
elif seed == 4:
    # Edge: single element, not found
    print("1 1")
    print("42")
    print("43")
elif seed == 5:
    # Edge: search for value less than min
    print("5 1")
    print("10 20 30 40 50")
    print("5")
elif seed == 6:
    # Edge: search for value greater than max
    print("5 1")
    print("10 20 30 40 50")
    print("55")
elif seed == 7:
    # Edge: large array, all found
    n = 1000
    a = sorted(random.sample(range(-10**6, 10**6), n))
    q = min(100, n)
    queries = random.sample(a, q)
    print(f"{n} {q}")
    print(" ".join(map(str, a)))
    for x in queries:
        print(x)
elif seed == 8:
    # Edge: large array, none found
    n = 1000
    a = sorted(random.sample(range(0, 2 * n, 2), n))  # all even
    q = 100
    queries = [random.randint(0, 2 * n) | 1 for _ in range(q)]  # all odd
    print(f"{n} {q}")
    print(" ".join(map(str, a)))
    for x in queries:
        print(x)
elif seed == 9:
    # Edge: max constraints
    n = 100000
    a = sorted(random.sample(range(-10**9, 10**9), n))
    q = 100000
    queries = []
    for _ in range(q):
        if random.random() < 0.5:
            queries.append(random.choice(a))
        else:
            queries.append(random.randint(-10**9, 10**9))
    print(f"{n} {q}")
    print(" ".join(map(str, a)))
    for x in queries:
        print(x)
else:
    n = random.randint(1, 100000)
    a = sorted(random.sample(range(-10**9, 10**9), n))
    q = random.randint(1, 100000)
    queries = []
    for _ in range(q):
        if random.random() < 0.5:
            queries.append(random.choice(a))
        else:
            queries.append(random.randint(-10**9, 10**9))
    print(f"{n} {q}")
    print(" ".join(map(str, a)))
    for x in queries:
        print(x)
