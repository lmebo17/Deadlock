#pragma once
#include <iostream>
#include <vector>
#include <string>
#include <queue>
#include <sstream>
#include <algorithm>
using namespace std;

struct ListNode {
    int val;
    ListNode* next;
    ListNode() : val(0), next(nullptr) {}
    ListNode(int x) : val(x), next(nullptr) {}
    ListNode(int x, ListNode* n) : val(x), next(n) {}
};

struct TreeNode {
    int val;
    TreeNode* left;
    TreeNode* right;
    TreeNode() : val(0), left(nullptr), right(nullptr) {}
    TreeNode(int x) : val(x), left(nullptr), right(nullptr) {}
    TreeNode(int x, TreeNode* l, TreeNode* r) : val(x), left(l), right(r) {}
};

namespace _json {
    string trim(const string& s) {
        size_t a = s.find_first_not_of(" \t\n\r");
        size_t b = s.find_last_not_of(" \t\n\r");
        return (a == string::npos) ? "" : s.substr(a, b - a + 1);
    }

    int parseInt(const string& s) { return stoi(trim(s)); }
    double parseFloat(const string& s) { return stod(trim(s)); }
    bool parseBool(const string& s) { return trim(s) == "true"; }

    string parseString(const string& s) {
        string t = trim(s);
        if (t.size() >= 2 && t[0] == '"' && t.back() == '"')
            return t.substr(1, t.size() - 2);
        return t;
    }

    vector<string> splitArray(const string& s) {
        string t = trim(s);
        if (t.size() < 2) return {};
        t = t.substr(1, t.size() - 2);
        vector<string> result;
        int depth = 0;
        string cur;
        for (char c : t) {
            if (c == '[') depth++;
            if (c == ']') depth--;
            if (c == ',' && depth == 0) {
                result.push_back(trim(cur));
                cur.clear();
            } else {
                cur += c;
            }
        }
        if (!cur.empty()) result.push_back(trim(cur));
        return result;
    }

    vector<int> parseIntArray(const string& s) {
        auto parts = splitArray(s);
        vector<int> r;
        for (auto& p : parts) if (!p.empty()) r.push_back(stoi(p));
        return r;
    }

    vector<string> parseStringArray(const string& s) {
        auto parts = splitArray(s);
        vector<string> r;
        for (auto& p : parts) r.push_back(parseString(p));
        return r;
    }

    vector<vector<int>> parseInt2DArray(const string& s) {
        auto parts = splitArray(s);
        vector<vector<int>> r;
        for (auto& p : parts) r.push_back(parseIntArray(p));
        return r;
    }

    ListNode* parseListNode(const string& s) {
        auto arr = parseIntArray(s);
        if (arr.empty()) return nullptr;
        ListNode* head = new ListNode(arr[0]);
        ListNode* cur = head;
        for (size_t i = 1; i < arr.size(); i++) {
            cur->next = new ListNode(arr[i]);
            cur = cur->next;
        }
        return head;
    }

    TreeNode* parseTreeNode(const string& s) {
        auto parts = splitArray(s);
        if (parts.empty() || parts[0] == "null") return nullptr;
        TreeNode* root = new TreeNode(stoi(parts[0]));
        queue<TreeNode*> q;
        q.push(root);
        size_t i = 1;
        while (!q.empty() && i < parts.size()) {
            TreeNode* node = q.front(); q.pop();
            if (i < parts.size() && parts[i] != "null") {
                node->left = new TreeNode(stoi(parts[i]));
                q.push(node->left);
            }
            i++;
            if (i < parts.size() && parts[i] != "null") {
                node->right = new TreeNode(stoi(parts[i]));
                q.push(node->right);
            }
            i++;
        }
        return root;
    }

    string serializeInt(int v) { return to_string(v); }
    string serializeFloat(double v) { ostringstream os; os << v; return os.str(); }
    string serializeBool(bool v) { return v ? "true" : "false"; }
    string serializeString(const string& v) { return "\"" + v + "\""; }

    string serializeIntArray(const vector<int>& v) {
        string r = "[";
        for (size_t i = 0; i < v.size(); i++) {
            if (i) r += ",";
            r += to_string(v[i]);
        }
        return r + "]";
    }

    string serializeStringArray(const vector<string>& v) {
        string r = "[";
        for (size_t i = 0; i < v.size(); i++) {
            if (i) r += ",";
            r += "\"" + v[i] + "\"";
        }
        return r + "]";
    }

    string serializeInt2DArray(const vector<vector<int>>& v) {
        string r = "[";
        for (size_t i = 0; i < v.size(); i++) {
            if (i) r += ",";
            r += serializeIntArray(v[i]);
        }
        return r + "]";
    }

    string serializeListNode(ListNode* node) {
        vector<int> r;
        while (node) { r.push_back(node->val); node = node->next; }
        return serializeIntArray(r);
    }

    string serializeTreeNode(TreeNode* root) {
        if (!root) return "[]";
        vector<string> r;
        queue<TreeNode*> q;
        q.push(root);
        while (!q.empty()) {
            TreeNode* n = q.front(); q.pop();
            if (n) {
                r.push_back(to_string(n->val));
                q.push(n->left);
                q.push(n->right);
            } else {
                r.push_back("null");
            }
        }
        while (!r.empty() && r.back() == "null") r.pop_back();
        string res = "[";
        for (size_t i = 0; i < r.size(); i++) {
            if (i) res += ",";
            res += r[i];
        }
        return res + "]";
    }
}
