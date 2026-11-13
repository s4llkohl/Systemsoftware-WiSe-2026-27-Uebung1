import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.List;

// Das Programm erwartet als Parameter zuerst den Dateipfad, danach eine regular expression zum Datentyp.
// Ich würde empfehlen ".*\.java" zu verwenden
public class Counter extends Thread {

	public static void main(String[] args) throws IOException {
		assert (args.length == 3);

		long startTime = System.nanoTime();



		long totalLines = countLinesInAllFiles(args[0], args[1], Integer.parseInt(args[2]));

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

	public static long countLinesInAllFiles(String folderPath, String regex, int numberOfThreads) throws IOException {
		Pattern pattern = Pattern.compile(regex);
		List<Path> files;

		try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
			files = paths.filter(Files::isRegularFile).filter(path -> pattern.matcher(path.toString()).matches()).toList();
		}

		AtomicLong totalLines = new AtomicLong(0);


		Thread[] threads = new Thread[numberOfThreads];

		for(int i = 0 ; i < numberOfThreads;i++){
			int threadId = i;
			threads[i] = new Thread(()->{


				for (int j = threadId; j < files.size(); j+=numberOfThreads){
					try{
						totalLines.addAndGet(countLines(files.get(j).toString()));
					} catch (IOException e){
						
					}

				}
			});
			threads[i].start();
		}

		for(int i = 0; i < numberOfThreads; i++)
		{
			try{
				threads[i].join();
			} catch (InterruptedException e){

			}

		}
		return totalLines.get();
	}
}
