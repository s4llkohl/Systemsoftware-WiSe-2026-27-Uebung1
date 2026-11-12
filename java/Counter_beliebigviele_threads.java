import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

// Das Programm erwartet als Parameter zuerst den Dateipfad, danach eine regular expression zum Datentyp.
// Ich würde empfehlen ".*\.java" zu verwenden
public class Counter_beliebigviele_threads {

	public static void main(String[] args) throws IOException {
		assert (args.length == 2);

		long startTime = System.nanoTime();
        int threa_anzahl = Integer.parseInt(args[2]);
        
		if(args.length != 3) {
			System.out.println("Bitte geben Sie einen Dateipfad und eine regular expression \noder einen Ordnerpfad und eine regular expression  \n und die Anzahl der Threads an.");
			return;
		}
        if(threa_anzahl < 1) {
            System.out.println("Bitte geben Sie eine positive Anzahl von Threads an.");
            return;
        }


		long totalLines = countLinesInAllFiles(args[0], args[1], threa_anzahl);
	
		long endTime = System.nanoTime();

		System.out.println("Anzahl Zeilen: " + totalLines);
		System.out.println("Benötigte Zeit: "
				+ (endTime - startTime) / 1_000_000.0 + " ms");
        System.out.println("Anzahl Threads: " + threa_anzahl+"\n");
	}



    public static List<File> getAllFieles (String folder, String regex){
        File[] files = new File(folder).listFiles();
        List<File> goodFilesList = new ArrayList<>();
        Pattern pattern = Pattern.compile(regex);
        for (File file : files) {
            if (file.isFile() && pattern.matcher(file.getName()).matches()) {
                goodFilesList.add(file);
            }
        }
        return goodFilesList;
    }

    public static long countLinesInAllFiles(String folder, String regex, int thread_zahl) throws IOException {
		AtomicLong size = new AtomicLong(0);
		Pattern pattern = Pattern.compile(regex);
		File[] files = new File(folder).listFiles();
        List<File> goodFilesList = new ArrayList<>();
        int filesPerThread = 1;
        int modthread = 0;
        List<Thread> threads = new ArrayList<>();

        for (File file : files) {
            if (file.isDirectory()) {
               goodFilesList.addAll(getAllFieles(file.getAbsolutePath(), regex));
            } else if (file.isFile() && pattern.matcher(file.getName()).matches()) {
                goodFilesList.add(file);
            }
        }
        if (goodFilesList.isEmpty()) {
            System.out.println("Keine Dateien gefunden, die dem Muster entsprechen.");
            return 0;
        }
        if (thread_zahl > goodFilesList.size()) {
            thread_zahl = goodFilesList.size();
        }
        //else if(goodFilesList.size() % thread_zahl != 0 && goodFilesList.size() / 2 < thread_zahl){
            //filesPerThread = (goodFilesList.size() / thread_zahl) + 1;
        //}
        else {
            filesPerThread = goodFilesList.size() / thread_zahl;
            modthread = goodFilesList.size() % thread_zahl; 
        }

		for (int i = 0; i < thread_zahl; i++) {
            List<File> filesForThreadList = new ArrayList<>();
            filesForThreadList = goodFilesList.subList(i * filesPerThread, (i + 1) * filesPerThread);    
			if(modthread > 0) {
                modthread--;
                filesForThreadList.add(goodFilesList.get(goodFilesList.size() - modthread - 1));
            }
            //System.out.println("Thread " + (i + 1) + " bearbeitet " + filesForThreadList.size() + " Dateien."); 
            File[] fielesForThread = filesForThreadList.toArray(new File[0]);
            
            Thread thread = new Thread(() -> {
				try {
                    for(File file : fielesForThread){
                        size.addAndGet(countLines(file.getAbsolutePath()));
                    }
                } catch (IOException e) {
						e.printStackTrace();
				}
			});
            thread.start();
            threads.add(thread);
        }
        for (Thread thread1 : threads) {
            try {
                thread1.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return size.get();
    }

    public static long countLines(String fileName) throws IOException {
		long lines = 0;

         try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            while (reader.readLine() != null) {
                lines++;
            }
        }
		return lines;
	}
    

}
