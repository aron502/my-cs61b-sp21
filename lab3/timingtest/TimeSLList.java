package timingtest;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeSLList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeGetLast();
    }

    public static void timeGetLast() {
        SLList<Integer> testList = new SLList<Integer>();
        int n = 1000;
        AList<Integer> ns = new AList<Integer>();
        AList<Double> times = new AList<Double>();
        AList<Integer> opCounts = new AList<Integer>();
        int counts = 10000;
        for (int i = 0; i < 8; i++) {
            for (int num = 0; num < n; num++) {
                testList.addLast(num);
            }
            ns.addLast(n);
            opCounts.addLast(counts);
            Stopwatch sw = new Stopwatch();
            for (int j = 0; j < counts; j++) {
                testList.getLast();
            }
            times.addLast(sw.elapsedTime());
            n *= 2;
        }
        printTimingTable(ns, times, opCounts);
    }

}
