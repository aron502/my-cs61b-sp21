package deque;

import org.junit.Test;

import java.util.Comparator;

import static org.junit.Assert.*;

public class MaxArrayDequeTest {
    @Test
    public void testComparator() {
        IntComparator cp = new IntComparator();
        MaxArrayDeque<Integer> mad = new MaxArrayDeque<>(cp);
        for (int i = 0; i < 10; i++) {
            mad.addLast(i);
        }
        int max = mad.max();
        assertEquals(max, 9);
    }

    @Test
    public void testReverseComparator() {
        IntComparator cp = new IntComparator();
        IntReverseComparator rcp = new IntReverseComparator();
        MaxArrayDeque<Integer> mad = new MaxArrayDeque<>(cp);
        for (int i = 0; i < 10; i++) {
            mad.addLast(i);
        }
        int max = mad.max(rcp);
        assertEquals(max, 0);
    }

    public class IntComparator implements Comparator<Integer> {
        @Override
        public int compare(Integer a, Integer b) {
            return a.compareTo(b);
        }
    }

    public class IntReverseComparator implements Comparator<Integer> {
        @Override
        public int compare(Integer a, Integer b) {
            return -(a.compareTo(b));
        }
    }
}
