import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ExecutionException;

// Das Programm erwartet als Parameter zuerst den Dateipfad, danach eine regular expression zum Datentyp.
// Ich würde empfehlen ".*\.java" zu verwenden
public class Counter {

	public static void main(String[] args) throws IOException {
		assert (args.length == 3);

		int numThreads = Integer.parseInt(args[2]);

		long startTime = System.nanoTime();

		long totalLines = countLinesInAllFiles(args[0], args[1], numThreads);

		long endTime = System.nanoTime();

		System.out.println("Anzahl Zeilen: " + totalLines);
		System.out.println("Benötigte Zeit: "
				+ (endTime - startTime) / 1_000_000.0 + " ms");
		System.out.println("Verwendete Threads: " + numThreads);
	}

	public static long countLines(String fileName) throws IOException {
		try (Stream<String> lines = Files.lines(Paths.get(fileName))) {
			return lines.count();
		}
	}

	public static long countLinesInAllFiles(String folderPath, String regex, int numThreads)
			throws IOException {
		Pattern pattern = Pattern.compile(regex);
		ForkJoinPool threads = new ForkJoinPool(numThreads);

		try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {

			return threads.submit(() ->
					paths
					.filter(Files::isRegularFile)
					.parallel()
					.filter(path -> pattern.matcher(path.toString()).matches())
					.mapToLong(path -> {
						try {
							return countLines(path.toString());
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}
					})
					.sum()
			).get();
		} catch (InterruptedException e){
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);

		} catch (ExecutionException e){
			throw new RuntimeException(e);

		} finally {
			threads.shutdown();
		}
	}
}
