package bstmap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class BSTMap<Key extends Comparable<Key>, Val> implements Map61B<Key, Val> {
    private BSTNode root;
    private class BSTNode {
        Key key;
        Val val;
        int n;
        BSTNode left;
        BSTNode right;
        BSTNode(Key key, Val val, int n) {
            this.key = key;
            this.val = val;
            this.n = n;
        }
    }

    public void clear() {
        root = null;
    }

    public boolean containsKey(Key key) {
        return containsKey(root, key);
    }
    private boolean containsKey(BSTNode head, Key key) {
        if (head == null) {
            return false;
        }
        int cmp = key.compareTo(head.key);
        if (cmp < 0) {
            return containsKey(head.left, key);
        } else if (cmp > 0) {
            return containsKey(head.right, key);
        } else {
            return true;
        }
    }

    public Val get(Key key) {
        return get(root, key);
    }
    private Val get(BSTNode head, Key key) {
        if (head == null) {
            return null;
        }
        int cmp = key.compareTo(head.key);
        if (cmp < 0) {
            return get(head.left, key);
        } else if (cmp > 0) {
            return get(head.right, key);
        } else {
            return head.val;
        }
    }

    public int size() {
        return size(root);
    }
    private int size(BSTNode head) {
        if (head == null) {
            return 0;
        }
        return head.n;
    }

    public void put(Key key, Val val) {
        root = put(root, key, val);
    }
    private BSTNode put(BSTNode head, Key key, Val val) {
        if (head == null) {
            return new BSTNode(key, val, 1);
        }
        int cmp = key.compareTo(head.key);
        if (cmp < 0) {
            head.left = put(head.left, key, val);
        } else if (cmp > 0) {
            head.right = put(head.right, key, val);
        } else {
            head.val = val;
        }
        head.n = size(head.left) + size(head.right) + 1;
        return head;
    }

    public void printInOrder() {
        printInOrder(root);
    }
    private void printInOrder(BSTNode head) {
        if (head == null) {
            return;
        }
        printInOrder(head.left);
        System.out.print(head.val + " ");
        printInOrder(head.right);
    }

    public Set<Key> keySet() {
        HashSet<Key> sets = new HashSet<>();
        keySet(sets, root);
        return sets;
    }
    private void keySet(Set<Key> sets, BSTNode head) {
        if (head == null) {
            return;
        }
        keySet(sets, head.left);
        sets.add(head.key);
        keySet(sets, head.right);
    }

    public Val remove(Key key) {
        if (!containsKey(key)) {
            return null;
        }
        return remove(root, key).val;
    }
    private BSTNode remove(BSTNode head, Key key) {
        if (head == null) {
            return null;
        }
        int cmp = key.compareTo(head.key);
        if (cmp < 0) {
            head.left = remove(head.left, key);
        } else if (cmp > 0) {
            head.right = remove(head.right, key);
        } else {
            if (head.left == null) {
                return head.right;
            }
            if (head.right == null) {
                return head.left;
            }
            BSTNode t = head;
            head = min(head.right);
            head.left = t.left;
            head.right = removeMin(head.right);
        }
        head.n = size(head.left) + size(head.right) + 1;
        return head;
    }

    public Val remove(Key key, Val val) {
        if (!containsKey(key)) {
            return null;
        }
        Val value = get(key);
        if (value != val) {
            return null;
        }
        return remove(key);
    }

    private BSTNode min(BSTNode head) {
        if (head.left == null) {
            return head;
        }
        return min(head.left);
    }

    private BSTNode removeMin(BSTNode head) {
        if (head.left == null) {
            return head.right;
        }
        head.left = removeMin(head.left);
        head.n = size(head.left) + size(head.right) + 1;
        return head;
    }

    public Iterator<Key> iterator() {
        return keySet().iterator();
    }

}
