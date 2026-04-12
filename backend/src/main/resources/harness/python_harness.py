import json, sys
from collections import deque

class ListNode:
    def __init__(self, val=0, next=None):
        self.val = val
        self.next = next

class TreeNode:
    def __init__(self, val=0, left=None, right=None):
        self.val = val
        self.left = left
        self.right = right

def _list_to_listnode(arr):
    if not arr: return None
    head = ListNode(arr[0])
    cur = head
    for v in arr[1:]:
        cur.next = ListNode(v)
        cur = cur.next
    return head

def _listnode_to_list(node):
    r = []
    while node:
        r.append(node.val)
        node = node.next
    return r

def _list_to_treenode(arr):
    if not arr or arr[0] is None: return None
    root = TreeNode(arr[0])
    q = deque([root])
    i = 1
    while q and i < len(arr):
        node = q.popleft()
        if i < len(arr) and arr[i] is not None:
            node.left = TreeNode(arr[i])
            q.append(node.left)
        i += 1
        if i < len(arr) and arr[i] is not None:
            node.right = TreeNode(arr[i])
            q.append(node.right)
        i += 1
    return root

def _treenode_to_list(root):
    if not root: return []
    r, q = [], deque([root])
    while q:
        node = q.popleft()
        if node:
            r.append(node.val)
            q.append(node.left)
            q.append(node.right)
        else:
            r.append(None)
    while r and r[-1] is None: r.pop()
    return r

def _deser(t, v):
    if t == "ListNode": return _list_to_listnode(v)
    if t == "TreeNode": return _list_to_treenode(v)
    return v

def _ser(t, v):
    if t == "ListNode": return _listnode_to_list(v)
    if t == "TreeNode": return _treenode_to_list(v)
    return v

# --- USER CODE ---
