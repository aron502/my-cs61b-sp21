package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private Comparator<T> cp;

    public MaxArrayDeque(Comparator<T> c) {
        super();
        cp = c;
    }

    public T max() {
        if (size() == 0) {
            return null;
        }
        T max_item = get(0);
        for (T item : this) {
            if (cp.compare(max_item, item) < 0) {
                max_item = item;
            }
        }
        return max_item;
    }

    public T max(Comparator<T> c) {
        if (size() == 0) {
            return null;
        }
        T max_item = get(0);
        for (T item : this) {
            if (c.compare(max_item, item) < 0) {
                max_item = item;
            }
        }
        return max_item;
    }
}
