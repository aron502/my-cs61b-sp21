package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;
public class TestBuggyAList {
    @Test
    public void testThreeAddRemove() {
        AListNoResizing<Integer> correct = new AListNoResizing<Integer>();
        BuggyAList<Integer> sus = new BuggyAList<Integer>();
        for (int i = 4; i <= 6; i++) {
            correct.addLast(i);
            sus.addLast(i);
        }
        assertEquals(correct.size(), sus.size());
        assertEquals(correct.removeLast(), sus.removeLast());
        assertEquals(correct.removeLast(), sus.removeLast());
        assertEquals(correct.removeLast(), sus.removeLast());
    }

    @Test
    public void randomTest() {
        AListNoResizing<Integer> L = new AListNoResizing<Integer>();
        BuggyAList<Integer> B = new BuggyAList<Integer>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                B.addLast(randVal);
                System.out.println("addLast(" + randVal + ")");
            } else if (operationNumber == 1) {
                // size
                assertEquals(L.size(), B.size());
            } else if (operationNumber == 2) {
                if (L.size() <= 0) {
                    continue;
                }
                assertEquals(L.getLast(), B.getLast());
            } else if (operationNumber == 3) {
                if (L.size() <= 0) {
                    continue;
                }
                assertEquals(L.removeLast(), B.removeLast());
            }
        }
    }
}
