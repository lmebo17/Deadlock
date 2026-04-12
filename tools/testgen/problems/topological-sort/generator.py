#!/usr/bin/env python3
import random, sys, json

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

    return [list(e) for e in edges]

if seed == 0:
    n = 4
    edges = [[1, 2], [1, 3], [2, 4], [3, 4]]
elif seed == 1:
    n = 3
    edges = [[3, 1], [3, 2]]
elif seed == 2:
    n = 5
    edges = [[5, 1], [5, 2], [4, 1], [2, 3]]
elif seed == 3:
    n = 1
    edges = []
elif seed == 4:
    n = 2
    edges = [[1, 2]]
elif seed == 5:
    n = 5
    edges = []
elif seed == 6:
    # Chain
    n = 100
    edges = [[i, i + 1] for i in range(1, n)]
elif seed == 7:
    # Reverse chain
    n = 100
    edges = [[i, i - 1] for i in range(n, 1, -1)]
elif seed == 8:
    # Star from vertex 1
    n = 100
    edges = [[1, i] for i in range(2, n + 1)]
elif seed == 9:
    # Large DAG
    n = 1000
    edges = gen_dag(n, 5000)
else:
    n = random.randint(1, 100000)
    max_m = min(200000, n * (n - 1) // 2)
    m = random.randint(0, min(max_m, 200000))
    if n > 1000:
        m = min(m, 200000)
    edges = gen_dag(n, m)

print(json.dumps(n))
print(json.dumps(edges))
