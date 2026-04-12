#!/usr/bin/env python3
import json, sys

s = json.loads(input())
print(json.dumps(s == s[::-1]))
