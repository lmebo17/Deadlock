#!/usr/bin/env python3
import random, sys, string

seed = int(sys.argv[1]) if len(sys.argv) > 1 else 42
random.seed(seed)

if seed == 0:
    print("racecar")
elif seed == 1:
    print("hello")
elif seed == 2:
    print("a")
elif seed == 3:
    # Edge: two same chars
    print("aa")
elif seed == 4:
    # Edge: two different chars
    print("ab")
elif seed == 5:
    # Edge: even length palindrome
    print("abba")
elif seed == 6:
    # Edge: odd length palindrome
    print("abcba")
elif seed == 7:
    # Edge: almost palindrome
    print("abcda")
elif seed == 8:
    # Edge: all same char
    print("a" * 100000)
elif seed == 9:
    # Edge: long palindrome
    half = "".join(random.choice(string.ascii_lowercase) for _ in range(50000))
    print(half + half[::-1])
else:
    n = random.randint(1, 100000)
    if random.random() < 0.3:
        # Generate palindrome
        half_len = (n + 1) // 2
        half = "".join(random.choice(string.ascii_lowercase) for _ in range(half_len))
        if n % 2 == 0:
            s = half + half[::-1]
        else:
            s = half + half[-2::-1]
        print(s)
    else:
        # Random string (unlikely palindrome)
        print("".join(random.choice(string.ascii_lowercase) for _ in range(n)))
