#!/usr/bin/env python3
import random, sys

seed = int(sys.argv[1]) if len(sys.argv) > 1 else 42
random.seed(seed)

def gen_unique_pair(n, target, idx_i, idx_j):
    """Generate array with unique solution at idx_i, idx_j summing to target."""
    a_i = random.randint(-10**9, 10**9)
    a_j = target - a_i
    if abs(a_j) > 10**9:
        a_i = 0
        a_j = target

    # Fill other positions with values that don't create another pair
    used = set()
    used.add(a_i)
    used.add(a_j)
    arr = [0] * n
    arr[idx_i] = a_i
    arr[idx_j] = a_j

    for k in range(n):
        if k == idx_i or k == idx_j:
            continue
        while True:
            v = random.randint(-10**9, 10**9)
            complement = target - v
            # Make sure v doesn't form a pair with any existing element
            ok = True
            if v in used or complement in used:
                # Could form unwanted pair; try different value
                if complement != v:
                    ok = False
                elif list(arr[:k]).count(v) + (1 if k > idx_i and arr[idx_i] == v else 0) >= 1:
                    ok = False
            if ok:
                arr[k] = v
                used.add(v)
                break
    return arr

if seed == 0:
    print("4 9")
    print("2 7 11 15")
elif seed == 1:
    print("3 6")
    print("3 2 3")
elif seed == 2:
    print("5 0")
    print("1 -1 2 3 4")
elif seed == 3:
    # Edge: n=2
    print("2 5")
    print("2 3")
elif seed == 4:
    # Edge: large values
    print("2 0")
    print("1000000000 -1000000000")
elif seed == 5:
    # Edge: pair at start and end
    print("5 10")
    print("3 1 2 4 7")
elif seed == 6:
    # Edge: negative target
    print("4 -5")
    print("-3 1 -2 4")
elif seed == 7:
    # Edge: zero elements
    print("3 0")
    print("0 5 0")
elif seed == 8:
    # Edge: large n, pair near end
    n = 1000
    arr = list(range(1, n + 1))
    # pair: arr[n-2] + arr[n-1] = (n-1) + n
    target = arr[n - 2] + arr[n - 1]
    print(f"{n} {target}")
    print(" ".join(map(str, arr)))
elif seed == 9:
    # Edge: pair at positions 1 and 2 in large array
    n = 1000
    arr = [500000000, 500000001] + [i for i in range(2, n)]
    target = 1000000001
    print(f"{n} {target}")
    print(" ".join(map(str, arr)))
else:
    n = random.randint(2, 100000)
    idx_i = random.randint(0, n - 2)
    idx_j = random.randint(idx_i + 1, n - 1)
    target = random.randint(-2 * 10**9, 2 * 10**9)
    # Simple approach: place the pair, fill rest with unique large negatives
    a_val = random.randint(-10**9, 10**9)
    b_val = target - a_val
    if abs(b_val) > 10**9:
        a_val = target // 2
        b_val = target - a_val
    arr = []
    pair_set = {a_val, b_val}
    for k in range(n):
        if k == idx_i:
            arr.append(a_val)
        elif k == idx_j:
            arr.append(b_val)
        else:
            # Use values far from any complement
            while True:
                v = random.randint(-10**9, 10**9)
                if target - v not in pair_set and v != a_val and v != b_val:
                    pair_set.add(v)
                    arr.append(v)
                    break
    print(f"{n} {target}")
    print(" ".join(map(str, arr)))
