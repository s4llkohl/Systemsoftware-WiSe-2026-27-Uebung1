import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// Parallelisierte Variante des Line Counters.
// Aufruf: java ParallelCounter <Ordner> <Regex> [Anzahl Threads]
// Beispiel: java ParallelCounter ./src ".*\.java" 4
// Ohne dritten Parameter wird die Anzahl verfügbarer Kerne verwendet.
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

		long totalLines = countLinesInAllFiles(args[0], args[1], threads);

		long endTime = System.nanoTime();

		System.out.println("Threads: " + threads);
		System.out.println("Anzahl Zeilen: " + totalLines);
		System.out.println("Benötigte Zeit: "
				+ (endTime - startTime) / 1_000_000.0 + " ms");
	}

	public static long countLines(Path file) throws IOException {
		try (Stream<String> lines = Files.lines(file)) {
			return lines.count();
		}
	}

	public static long countLinesInAllFiles(String folderPath, String regex, int threads)
			throws IOException, InterruptedException, ExecutionException {
		Pattern pattern = Pattern.compile(regex);

		// 1. Schritt (sequentiell): alle passenden Dateien einsammeln
		List<Path> files;
		try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
			files = paths
					.filter(Files::isRegularFile)
					.filter(path -> pattern.matcher(path.toString()).matches())
					.collect(Collectors.toList());
		}

		// 2. Schritt (parallel): Dateien gleichmäßig auf die Threads verteilen.
		// Jeder Thread zählt seinen Teil und liefert eine Teilsumme zurück,
		// dadurch ist keine Synchronisation auf einer gemeinsamen Variable nötig.
		ExecutorService pool = Executors.newFixedThreadPool(threads);
		try {
			List<Future<Long>> results = new ArrayList<>();
			for (int t = 0; t < threads; t++) {
				final int id = t;
				results.add(pool.submit(() -> {
					long sum = 0;
					// Round-Robin: Thread id bearbeitet die Dateien id, id+threads, ...
					for (int i = id; i < files.size(); i += threads) {
						try {
							sum += countLines(files.get(i));
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}
					}
					return sum;
				}));
			}

			// 3. Schritt: Teilsummen zusammenführen (join)
			long total = 0;
			for (Future<Long> f : results) {
				total += f.get();
			}
			return total;
		} finally {
			pool.shutdown();
		}
	}
}
