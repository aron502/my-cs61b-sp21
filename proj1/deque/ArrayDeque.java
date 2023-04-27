package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    private static final int INITIAL_OPACITY = 8;
    private T[] a;
    private int size;
    private int head;
    private int tail;

    public ArrayDeque() {
        a = (T[]) new Object[INITIAL_OPACITY];
        size = 0;
        head = 0;
        tail = 0;
    }

    public int size() {
        return size;
    }

    public void addFirst(T item) {
        checkFull(size);
        head = getIndex(head, -1);
        a[head] = item;
        size++;
    }

    public void addLast(T item) {
        checkFull(size);
        a[tail] = item;
        tail = getIndex(tail, 1);
        size++;
    }

    public void printDeque() {
        for (int i = 0; i < size; i++) {
            System.out.print(a[getIndex(head, i)] + " ");
        }
        System.out.println();
    }

    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        T item = a[head];
        head = getIndex(head, 1);
        size--;
        checkLessQuarter(size);
        return item;
    }

    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        tail = getIndex(tail, -1);
        T item = a[tail];
        size--;
        checkLessQuarter(size);
        return item;
    }

    public T get(int index) {
        if (index >= size) {
            return null;
        }
        return a[getIndex(head, index)];
    }

    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }
    private class ArrayDequeIterator implements Iterator<T> {
        private int index;

        public ArrayDequeIterator() {
            index = 0;
        }
        public boolean hasNext() {
            return index < size;
        }
        public T next() {
            return get(index++);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (T item : this) {
            sb.append(item);
            sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this == o) {
            return true;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }
        ArrayDeque<T> rhs = (ArrayDeque<T>) o;
        if (this.size() != rhs.size()) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (get(i) != rhs.get(i)) {
                return false;
            }
        }
        return true;
    }

    private void resize(int opacity) {
        T[] newArray = (T[]) new Object[opacity];
        for (int i = 0; i < size; i++) {
            newArray[i] = a[getIndex(head, i)];
        }
        head = 0;
        tail = size;
        a = newArray;
    }

    private int getIndex(int index, int offset) {
        return (index + offset + a.length) % a.length;
    }


    private void checkFull(int size) {
        if (a.length == size) {
            resize(2 * size);
        }
    }

    private void checkLessQuarter(int size) {
        if (size > 4 && size < a.length / 4) {
            resize(a.length / 4);
        }
    }
}