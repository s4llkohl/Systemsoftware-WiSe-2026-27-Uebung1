import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;


public class Benchmark {

	static final int[] THREADS = {1, 2, 4, 8, 16, 32, 64};
	static final int RUNS = 5;

	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.out.println("Aufruf: java Benchmark <Ordner> <Regex>");
			return;
		}
		System.out.println("Kerne laut Java: " + Runtime.getRuntime().availableProcessors());

		// Aufwärm-Lauf
		List<Path> files = ParallelCounter.collectFiles(args[0], args[1]);
		long lines = ParallelCounter.countLinesParallel(files, 1);
		System.out.println("Dateien: " + files.size() + ", Zeilen: " + lines);
		System.out.println();
		System.out.println("Threads | Zeit (ms) | Speedup");
		System.out.println("--------+-----------+--------");

		double timeOneThread = 0;
		for (int t : THREADS) {
			double[] times = new double[RUNS];
			for (int r = 0; r < RUNS; r++) {
				long start = System.nanoTime();
				List<Path> f = ParallelCounter.collectFiles(args[0], args[1]);
				long result = ParallelCounter.countLinesParallel(f, t);
				times[r] = (System.nanoTime() - start) / 1_000_000.0;
				if (result != lines) {
					System.out.println("FEHLER: falsche Zeilenzahl " + result);
				}
			}
			Arrays.sort(times);
			double median = times[RUNS / 2];
			if (t == 1) {
				timeOneThread = median;
			}
			System.out.printf("%7d | %9.1f | %6.2f%n", t, median, timeOneThread / median);
		}
	}
}
