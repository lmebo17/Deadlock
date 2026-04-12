#!/usr/bin/env python3
import random, sys

seed = int(sys.argv[1]) if len(sys.argv) > 1 else 42
random.seed(seed)

def gen_dag(n, m):
    """Generate a DAG with n vertices and m edges. Edges go from lower to higher in a random permutation."""
    perm = list(range(1, n + 1))
    random.shuffle(perm)
    rank = [0] * (n + 1)
    for i, v in enumerate(perm):
        rank[v] = i

    edges = set()
    attempts = 0
    while len(edges) < m and attempts < m * 10:
        u = random.randint(1, n)
        v = random.randint(1, n)
        if u != v and rank[u] < rank[v] and (u, v) not in edges:
            edges.add((u, v))
        attempts += 1

    return list(edges)

if seed == 0:
    print("4 4")
    print("1 2")
    print("1 3")
    print("2 4")
    print("3 4")
elif seed == 1:
    print("3 2")
    print("3 1")
    print("3 2")
elif seed == 2:
    print("5 4")
    print("5 1")
    print("5 2")
    print("4 1")
    print("2 3")
elif seed == 3:
    # Edge: single vertex
    print("1 0")
elif seed == 4:
    # Edge: two vertices, one edge
    print("2 1")
    print("1 2")
elif seed == 5:
    # Edge: no edges (all independent)
    print("5 0")
elif seed == 6:
    # Edge: chain
    n = 100
    print(f"{n} {n - 1}")
    for i in range(1, n):
        print(f"{i} {i + 1}")
elif seed == 7:
    # Edge: reverse chain
    n = 100
    print(f"{n} {n - 1}")
    for i in range(n, 1, -1):
        print(f"{i} {i - 1}")
elif seed == 8:
    # Edge: star from vertex 1
    n = 100
    print(f"{n} {n - 1}")
    for i in range(2, n + 1):
        print(f"1 {i}")
elif seed == 9:
    # Edge: large DAG
    n = 1000
    m = 5000
    edges = gen_dag(n, m)
    m = len(edges)
    print(f"{n} {m}")
    for u, v in edges:
        print(f"{u} {v}")
else:
    n = random.randint(1, 100000)
    max_m = min(200000, n * (n - 1) // 2)
    m = random.randint(0, min(max_m, 200000))
    # For large n, limit m to be feasible
    if n > 1000:
        m = min(m, 200000)
    edges = gen_dag(n, m)
    m = len(edges)
    print(f"{n} {m}")
    for u, v in edges:
        print(f"{u} {v}")
