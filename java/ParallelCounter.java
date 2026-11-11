import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// Aufruf: java ParallelCounter <Ordner> <Regex> [Threads]
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
			throws Exception {
		Pattern pattern = Pattern.compile(regex);

		List<Path> files;
		try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
			files = paths
					.filter(Files::isRegularFile)
					.filter(path -> pattern.matcher(path.toString()).matches())
					.collect(Collectors.toList());
		}

		AtomicInteger next = new AtomicInteger(0);
		ExecutorService pool = Executors.newFixedThreadPool(threads);
		try {
			List<Future<Long>> results = new java.util.ArrayList<>();
			for (int t = 0; t < threads; t++) {
				results.add(pool.submit(() -> {
					long sum = 0;
					int i;
					while ((i = next.getAndIncrement()) < files.size()) {
						try {
							sum += countLines(files.get(i));
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}
					}
					return sum;
				}));
			}

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
