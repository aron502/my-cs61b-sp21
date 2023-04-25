package deque;

import java.util.Iterator;

// tow sentinel node implementation
public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {
    private Node sentinel = new Node(null, null, null);
    private Node last = new Node(null, null, null);
    private int size;

    private class Node {
        public T item;
        public Node prev;
        public Node next;
        public Node(T item, Node prev, Node next) {
            this.item = item;
            this.prev = prev;
            this.next = next;
        }

    }

    public LinkedListDeque() {
        sentinel.next = last;
        last.prev = sentinel;
        size = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size() == 0;
    }


    public void addFirst(T item) {
        sentinel.next = new Node(item, sentinel, sentinel.next);
        Node after = sentinel.next.next; // after the new node
        after.prev = sentinel.next;
        size += 1;
    }

    public void addLast(T item) {
        last.prev = new Node(item, last.prev, last);
        Node previous = last.prev.prev; // previous the new node
        previous.next = last.prev;
        size += 1;
    }

    public void printDeque() {
        Node head = sentinel.next;
        while (head != last) {
            System.out.print(head.item + " ");
            head = head.next;
        }
        System.out.println();
    }

    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        T item = sentinel.next.item;
        Node second = sentinel.next.next;
        sentinel.next = second;
        second.prev = sentinel;

        size -= 1;
        return item;
    }

    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        T item = last.prev.item;
        Node secondToLast = last.prev.prev;
        secondToLast.next = last;
        last.prev = secondToLast;

        size -= 1;
        return item;
    }

    public T get(int index) {
        if (index < 0 || index > size() - 1) {
            return null;
        }
        Node head = sentinel.next;
        for (int i = 0; i < index; i++) {
            head = head.next;
        }
        return head.item;
    }


    public T getRecursive(int index) {
        if (index < 0 || index > size() - 1) {
            return null;
        }
        return getRecursiveHelper(index, sentinel.next);
    }

    private T getRecursiveHelper(int index, Node head) {
        if (index == 0) {
            return head.item;
        }
        return getRecursiveHelper(index - 1, head.next);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }
        if (!(other instanceof LinkedListDeque)) {
            return false;
        }
        LinkedListDeque<T> rhs = (LinkedListDeque<T>) other;
        if (this.size() != rhs.size()) {
            return false;
        }
        for (int i = 0; i < size(); i++) {
            if (get(i) != rhs.get(i)) {
                return false;
            }
        }
        return true;
    }

    public Iterator<T> iterator() {
        return new LinkedListDequeIterator();
    }

    private class LinkedListDequeIterator implements Iterator<T> {
        private Node head;
        LinkedListDequeIterator() {
            head = sentinel.next;
        }
        public boolean hasNext() {
            return head != last;
        }
        public T next() {
            T item = head.item;
            head = head.next;
            return item;
        }

        public void remove() {

        }
    }



}
