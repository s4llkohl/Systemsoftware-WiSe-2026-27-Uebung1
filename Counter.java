import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

// Das Programm erwartet als Parameter zuerst den Dateipfad, danach eine regular expression zum Datentyp.
// Ich würde empfehlen ".*\.java" zu verwenden
public class Counter {

	long totalLines = 0;

	public static void main(String[] args) throws IOException, InterruptedException {

		assert (args.length == 2 || args.length == 3);
		int threadCount = args.length == 3 ? Integer.parseInt(args[2]) : 2;

		long startTime = System.nanoTime();

		long result = countLinesInAllFiles(args[0], args[1], threadCount);

		long endTime = System.nanoTime();

		System.out.println("Anzahl Zeilen: " + result);
		System.out.println("Benötigte Zeit: "
				+ (endTime - startTime) / 1_000_000.0 + " ms");
	}

	public static long countLines(String fileName) throws IOException {
		try (Stream<String> lines = Files.lines(Paths.get(fileName))) {
			return lines.count();
		}
	}

	public static long countLinesInAllFiles(String folderPath, String regex, int threadCount)
			throws IOException, InterruptedException {
		Pattern pattern = Pattern.compile(regex);
		Counter counter = new Counter();
		List<Thread> threadsList = new ArrayList<>();
		List<Path> filesList;

		try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
			filesList = paths
					.filter(Files::isRegularFile)
					.filter(path -> pattern.matcher(path.toString()).matches())
					.toList();
		}

		for (int i = 0; i < threadCount; i++) {
			int j = i;
			Thread thread = new Thread(() -> {
				for (int k = j; k < filesList.size(); k += threadCount) {
					try {
						long lines = countLines(filesList.get(k).toString());

						counter.totalLines += lines;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			threadsList.add(thread);
			thread.start();
		}

		for (Thread thread : threadsList) {
			thread.join();
		}
		return counter.totalLines;
	}
}
