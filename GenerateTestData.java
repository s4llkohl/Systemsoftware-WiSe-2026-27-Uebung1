import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;


public class GenerateTestData {

	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			System.out.println("Aufruf: java GenerateTestData <Zielordner> [Anzahl Dateien]");
			return;
		}
		Path root = Paths.get(args[0]);
		int fileCount = args.length >= 2 ? Integer.parseInt(args[1]) : 2000;

		Random random = new Random(42); // fester Seed -> bei jedem Lauf dieselben Daten
		String line = "    int wert = berechne(a, b) * 2; // Testzeile fuer den Line Counter";
		long totalLines = 0;

		for (int i = 0; i < fileCount; i++) {
			Path dir = root.resolve("ordner" + (i % 20));
			Files.createDirectories(dir);

			int lines = 500 + random.nextInt(4501); // 500 bis 5000 Zeilen
			totalLines += lines;
			try (BufferedWriter w = Files.newBufferedWriter(dir.resolve("Datei" + i + ".java"), StandardCharsets.UTF_8)) {
				for (int l = 0; l < lines; l++) {
					w.write(line);
					w.newLine();
				}
			}
			if (i % 100 == 0) {
				Files.writeString(dir.resolve("notiz" + i + ".txt"), "wird nicht gezaehlt\n".repeat(100));
			}
		}

		System.out.println(fileCount + " .java-Dateien in " + root.toAbsolutePath() + " erzeugt");
		System.out.println("Erwartete Anzahl Zeilen: " + totalLines);
	}
}
