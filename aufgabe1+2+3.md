## Aufgabe 1

**Programm:** Ein Programm besteht aus gespeicherten Anweisungen, die bei der Ausführung als Prozess gestartet werden können.

**Prozess:** Ein Prozess ist eine laufende Instanz eines Programms. Er besitzt einen eigenen Speicherbereich und kann mehrere Threads enthalten.

**Thread:** Ein Thread ist eine Ausführungseinheit innerhalb eines Prozesses. Mehrere Threads eines Prozesses können parallel arbeiten und teilen sich dessen Speicher und Ressourcen.

## Aufgabe 2

### a) Grenzen der Beschleunigung

Zusätzliche CPU-Kerne können einen Prozess nur begrenzt beschleunigen, da nicht alle Programmteile parallel ausgeführt werden können. Eine höhere Taktfrequenz ist durch Stromverbrauch und Wärmeentwicklung begrenzt.

### b) Amdahlsches Gesetz

Formel: S(n) = 1 / ((1 - p) + p / n)

Dabei ist p der parallelisierbare Anteil und n die Anzahl der Kerne.

**Beispielrechnung für 75 % und 4 Kerne:**

S(4) = 1 / ((1 - 0,75) + 0,75 / 4)

S(4) = 1 / (0,25 + 0,1875) = 1 / 0,4375 ≈ 2,29

**Ergebnisse:**

25 % parallelisierbar:
- 2 Kerne: 1,14
- 4 Kerne: 1,23
- 8 Kerne: 1,28
- 16 Kerne: 1,31

50 % parallelisierbar:
- 2 Kerne: 1,33
- 4 Kerne: 1,60
- 8 Kerne: 1,78
- 16 Kerne: 1,88

75 % parallelisierbar:
- 2 Kerne: 1,60
- 4 Kerne: 2,29
- 8 Kerne: 2,91
- 16 Kerne: 3,37

## Aufgabe 3

Als Testverzeichnis wurde der Ordner `./java` des Repositories verwendet, da kein größeres Java-Projekt lokal vorhanden war.

Originalprogramm:
- Anzahl Zeilen: 51
- Benötigte Zeit: 86,24925 ms

Anschließend wurde die parallelisierte Variante mit unterschiedlichen Thread-Anzahlen getestet.

Alle folgenden Versuche zählten 112 Zeilen.

- 1 Thread: 100,961875 ms
- 2 Threads: 94,885417 ms
- 3 Threads: 94,051208 ms
- 4 Threads: 88,283875 ms
- 5 Threads: 94,295333 ms
- 6 Threads: 93,612125 ms

### Beobachtung

Es sind keine eindeutigen Leistungsunterschiede zwischen den verschiedenen Thread-Anzahlen erkennbar. Vermutlich liegt dies daran, dass das Testverzeichnis nur zwei Java-Dateien enthält und somit zu wenig Arbeit für eine sinnvolle Parallelisierung vorhanden ist.