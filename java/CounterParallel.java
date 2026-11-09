import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;



// Das Programm erwartet als Parameter zuerst den Dateipfad, danach eine regular expression zum Datentyp.
// Ich würde empfehlen ".*\.java" zu verwenden
public class CounterParallel {

	public static void main(String[] args) throws IOException {
		assert (args.length == 3);

		long startTime = System.nanoTime();

		long totalLines = countLinesInAllFiles(args[0], args[1], args[2]);

		long endTime = System.nanoTime();

		System.out.println("Anzahl Zeilen: " + totalLines);
		System.out.println("Benötigte Zeit: "
				+ (endTime - startTime) / 1_000_000.0 + " ms");
	}

	public static long countLines(String fileName) throws IOException {
		try (Stream<String> lines = Files.lines(Paths.get(fileName))) {
			return lines.count();
		}
	}

	public static long countLinesInAllFiles(String folderPath, String regex, String threadCountStr)
			throws IOException {
		Pattern pattern = Pattern.compile(regex);
		int threadCount = Integer.parseInt(threadCountStr);

		try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
			List<Path> files = paths
					.filter(Files::isRegularFile)
					.filter(path -> pattern.matcher(path.toString()).matches())
					.toList();

			long[] partialCounts = new long[threadCount];
			int filesPerThread = (files.size() + threadCount - 1) / threadCount;

			List<Thread> threads = new java.util.ArrayList<>();
			for(int i = 0; i < threadCount; i++) {
				int start = i * filesPerThread;
				int end = Math.min(start + filesPerThread, files.size());
				if (start >= end) {
					break;
				}
				List<Path> assignedFiles = files.subList(start, end);
				int threadIndex = i;
				Thread thread = new Thread(() -> {
					long count = 0;

					for (Path file : assignedFiles) {
                        try {
                            count += countLines(file.toString());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
					partialCounts[threadIndex] = count;
				});
				threads.add(thread);
			}
			for (Thread thread : threads) {
				thread.start();
			}
			for (Thread thread : threads) {
				try {
					thread.join();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt(); // restore interruption status
					throw new RuntimeException("Thread was interrupted", e);
				}
			}
			return java.util.Arrays.stream(partialCounts).sum();
		}
	}
}
