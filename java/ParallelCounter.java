
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ParallelCounter {

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.out.println("Nutzung: <Ordner> <Regex> <Threads>");
            return;
        }

        long startTime = System.nanoTime();

        int threads = Integer.parseInt(args[2]);
        long totalLines = countLinesInAllFiles(args[0], args[1], threads);

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

    public static long countLinesInAllFiles(String folderPath, String regex, int threads)
            throws Exception {

        Pattern pattern = Pattern.compile(regex);

        try (ExecutorService executor = Executors.newFixedThreadPool(threads);
             Stream<Path> paths = Files.walk(Paths.get(folderPath))) {

            List<Future<Long>> results = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> pattern.matcher(path.toString()).matches())
                    .map(path -> executor.submit(() -> countLines(path.toString())))
                    .toList();

            long total = 0;
            for (Future<Long> result : results) {
                total += result.get();
            }

            return total;
        }
    }
}
