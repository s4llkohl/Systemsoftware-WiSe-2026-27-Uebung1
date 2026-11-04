import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CounterThreads {

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("Bitte gib 3 Argumente an: [Pfad] [Regex] [Anzahl Threads]");
            return;
        }

        String folderPath = args[0];
        String regex = args[1];
        int numThreads = Integer.parseInt(args[2]);

        long startTime = System.nanoTime();

        long totalLines = countLinesInAllFilesWithThreads(folderPath, regex, numThreads);

        long endTime = System.nanoTime();

        System.out.println("Verwendete Threads: " + numThreads);
        System.out.println("Anzahl Zeilen: " + totalLines);
        System.out.println("Benötigte Zeit: " + (endTime - startTime) / 1_000_000.0 + " ms");
    }

    public static long countLines(String fileName) throws IOException {
        try (Stream<String> lines = Files.lines(Paths.get(fileName))) {
            return lines.count();
        }
    }

    public static long countLinesInAllFilesWithThreads(String folderPath, String regex, int numThreads)
            throws Exception {
        Pattern pattern = Pattern.compile(regex);

        List<Path> files;
        try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
            files = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> pattern.matcher(path.toString()).matches())
                    .collect(Collectors.toList());
        }

        if (files.isEmpty()) return 0;

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<Long>> futures = new ArrayList<>();

        for (Path file : files) {
            Callable<Long> task = () -> countLines(file.toString());
            futures.add(executor.submit(task));
        }

        long totalLines = 0;
        for (Future<Long> future : futures) {
            totalLines += future.get();
        }

        executor.shutdown();
        return totalLines;
    }
}