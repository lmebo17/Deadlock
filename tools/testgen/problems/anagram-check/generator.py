#!/usr/bin/env python3
import random, sys, string, json

seed = int(sys.argv[1]) if len(sys.argv) > 1 else 42
random.seed(seed)

if seed == 0:
    s = "listen"
    t = "silent"
elif seed == 1:
    s = "hello"
    t = "world"
elif seed == 2:
    s = "aabb"
    t = "bbaa"
elif seed == 3:
    s = "a"
    t = "a"
elif seed == 4:
    s = "a"
    t = "b"
elif seed == 5:
    s = "abc"
    t = "abcd"
elif seed == 6:
    s = "abcdef"
    t = "abcdef"
elif seed == 7:
    s = "aaaa"
    t = "aaaa"
elif seed == 8:
    s = "aaa"
    t = "aaaa"
elif seed == 9:
    n = 100000
    s_list = list("".join(random.choice(string.ascii_lowercase) for _ in range(n)))
    t_list = s_list[:]
    random.shuffle(t_list)
    s = "".join(s_list)
    t = "".join(t_list)
else:
    n = random.randint(1, 100000)
    if random.random() < 0.4:
        s_list = list("".join(random.choice(string.ascii_lowercase) for _ in range(n)))
        t_list = s_list[:]
        random.shuffle(t_list)
        s = "".join(s_list)
        t = "".join(t_list)
    elif random.random() < 0.5:
        m = random.randint(1, 100000)
        s = "".join(random.choice(string.ascii_lowercase) for _ in range(n))
        t = "".join(random.choice(string.ascii_lowercase) for _ in range(m))
    else:
        s = "".join(random.choice(string.ascii_lowercase) for _ in range(n))
        t = "".join(random.choice(string.ascii_lowercase) for _ in range(n))

print(json.dumps(s))
print(json.dumps(t))
