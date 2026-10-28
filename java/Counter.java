import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.stream.Stream;

// Das Programm erwartet als Parameter zuerst den Dateipfad, danach eine regular expression zum Datentyp.
// Ich würde empfehlen ".*\.java" zu verwenden
public class Counter {

	public static void main(String[] args) throws IOException {
		assert (args.length == 2);

		long startTime = System.nanoTime();

		long totalLines = countLinesInAllFiles(args[0], args[1]);

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

	public static long countLinesInAllFiles(String folderPath, String regex)
			throws IOException {
		Pattern pattern = Pattern.compile(regex);

		try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
			return paths
					.filter(Files::isRegularFile)
					.filter(path -> pattern.matcher(path.toString()).matches())
					.mapToLong(path -> {
						try {
							return countLines(path.toString());
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}
					})
					.sum();
		}
	}
}
