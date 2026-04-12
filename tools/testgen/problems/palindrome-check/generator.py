#!/usr/bin/env python3
import random, sys, string, json

seed = int(sys.argv[1]) if len(sys.argv) > 1 else 42
random.seed(seed)

if seed == 0:
    s = "racecar"
elif seed == 1:
    s = "hello"
elif seed == 2:
    s = "a"
elif seed == 3:
    s = "aa"
elif seed == 4:
    s = "ab"
elif seed == 5:
    s = "abba"
elif seed == 6:
    s = "abcba"
elif seed == 7:
    s = "abcda"
elif seed == 8:
    s = "a" * 100000
elif seed == 9:
    half = "".join(random.choice(string.ascii_lowercase) for _ in range(50000))
    s = half + half[::-1]
else:
    n = random.randint(1, 100000)
    if random.random() < 0.3:
        half_len = (n + 1) // 2
        half = "".join(random.choice(string.ascii_lowercase) for _ in range(half_len))
        if n % 2 == 0:
            s = half + half[::-1]
        else:
            s = half + half[-2::-1]
    else:
        s = "".join(random.choice(string.ascii_lowercase) for _ in range(n))

print(json.dumps(s))
