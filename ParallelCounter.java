import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ParallelCounter {

	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.err.println("Aufruf: java ParallelCounter <Ordner> <Regex> [Threads]");
			System.exit(1);
		}
		int threads = args.length >= 3
				? Integer.parseInt(args[2])
				: Runtime.getRuntime().availableProcessors();

		long startTime = System.nanoTime();

		List<Path> files = collectFiles(args[0], args[1]);
		long walkTime = System.nanoTime();

		long totalLines = countLinesParallel(files, threads);

		long endTime = System.nanoTime();

		System.out.println("Threads: " + threads + ", Dateien: " + files.size());
		System.out.println("Anzahl Zeilen: " + totalLines);
		System.out.println("Dateisuche (seriell): " + (walkTime - startTime) / 1_000_000.0 + " ms");
		System.out.println("Benötigte Zeit: " + (endTime - startTime) / 1_000_000.0 + " ms");
	}

	public static List<Path> collectFiles(String folderPath, String regex) throws IOException {
		Pattern pattern = Pattern.compile(regex);
		try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
			return paths
					.filter(Files::isRegularFile)
					.filter(path -> pattern.matcher(path.toString()).matches())
					.collect(Collectors.toList());
		}
	}

	public static long countLines(Path file) throws IOException {
		try (Stream<String> lines = Files.lines(file)) {
			return lines.count();
		}
	}

	public static long countLinesParallel(List<Path> files, int threadCount)
			throws InterruptedException {
		AtomicInteger nextIndex = new AtomicInteger(0);
		long[] partialSums = new long[threadCount];
		Thread[] workers = new Thread[threadCount];

		for (int t = 0; t < threadCount; t++) {
			final int id = t;
			workers[t] = new Thread(() -> {
				long local = 0;
				int i;
				while ((i = nextIndex.getAndIncrement()) < files.size()) {
					try {
						local += countLines(files.get(i));
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				}
				partialSums[id] = local; // jeder Thread schreibt nur sein eigenes Feld
			});
			workers[t].start();
		}

		long total = 0;
		for (int t = 0; t < threadCount; t++) {
			workers[t].join(); // join() garantiert Sichtbarkeit von partialSums[t]
			total += partialSums[t];
		}
		return total;
	}
}
