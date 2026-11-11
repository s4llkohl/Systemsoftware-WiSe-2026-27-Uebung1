import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Counter {

    public static void main(String[] args) throws IOException {

        assert (args.length == 3);

        long startTime = System.nanoTime();

        long totalLines = countLinesInAllFiles(
                args[0], args[1], Integer.parseInt(args[2]));

        long endTime = System.nanoTime();

        System.out.println("Anzahl Zeilen: " + totalLines);
        System.out.println("Verwendete Threads: " + args[2]);
        System.out.println("Benötigte Zeit: "
                + (endTime - startTime) / 1_000_000.0 + " ms");
    }

    public static long countLines(String fileName) throws IOException {
        try (Stream<String> lines = Files.lines(Paths.get(fileName))) {
            return lines.count();
        }
    }

    public static long countLinesInAllFiles(
            String folderPath, String regex, int numberOfThreads)
            throws IOException {

        Pattern pattern = Pattern.compile(regex);

        ForkJoinPool pool = new ForkJoinPool(numberOfThreads);

        try {
            return pool.submit(() -> {

                try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
                    return paths
                            .parallel()
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
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }

            }).get();

        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
