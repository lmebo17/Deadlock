#!/usr/bin/env python3
import json, sys
from collections import Counter

s = json.loads(input())
t = json.loads(input())

print(json.dumps(Counter(s) == Counter(t)))
