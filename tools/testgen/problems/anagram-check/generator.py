#!/usr/bin/env python3
import random, sys, string

seed = int(sys.argv[1]) if len(sys.argv) > 1 else 42
random.seed(seed)

if seed == 0:
    print("listen")
    print("silent")
elif seed == 1:
    print("hello")
    print("world")
elif seed == 2:
    print("aabb")
    print("bbaa")
elif seed == 3:
    # Edge: single char same
    print("a")
    print("a")
elif seed == 4:
    # Edge: single char different
    print("a")
    print("b")
elif seed == 5:
    # Edge: different lengths
    print("abc")
    print("abcd")
elif seed == 6:
    # Edge: same string
    print("abcdef")
    print("abcdef")
elif seed == 7:
    # Edge: all same char
    print("aaaa")
    print("aaaa")
elif seed == 8:
    # Edge: all same char but different count
    print("aaa")
    print("aaaa")
elif seed == 9:
    # Edge: long anagram
    n = 100000
    s = list("".join(random.choice(string.ascii_lowercase) for _ in range(n)))
    t = s[:]
    random.shuffle(t)
    print("".join(s))
    print("".join(t))
else:
    n = random.randint(1, 100000)
    if random.random() < 0.4:
        # Generate anagram
        s = list("".join(random.choice(string.ascii_lowercase) for _ in range(n)))
        t = s[:]
        random.shuffle(t)
        print("".join(s))
        print("".join(t))
    elif random.random() < 0.5:
        # Different lengths
        m = random.randint(1, 100000)
        print("".join(random.choice(string.ascii_lowercase) for _ in range(n)))
        print("".join(random.choice(string.ascii_lowercase) for _ in range(m)))
    else:
        # Same length but not anagram
        print("".join(random.choice(string.ascii_lowercase) for _ in range(n)))
        print("".join(random.choice(string.ascii_lowercase) for _ in range(n)))
