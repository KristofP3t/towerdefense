# Tower Defense

Ein Tower-Defense-Spiel entwickelt mit Kotlin Multiplatform und Compose Multiplatform, verfügbar für Android, iOS, Web (JS + WASM) und Desktop (JVM).

---

## Spielprinzip

Verteidige deine Basis gegen Wellen von Feinden, indem du Türme auf dem Spielfeld platzierst. Besiege alle 15 Wellen – inklusive Boss-Wellen alle 5 Runden – um zu gewinnen.

---

## Aktueller Spielstand (implementierte Features)

### Türme (3 Typen)
| Typ | Schaden | Reichweite | Feuerrate | Kosten | Besonderheit |
|-----|---------|------------|-----------|--------|--------------|
| ROT (Rot) | 55 | 144 px | 1,2 s | 75 G | Hoher Schaden |
| GELB | 20 | 240 px | 1,6 s | 50 G | Größte Reichweite |
| BLAU | 12 | 160 px | 2,0 s | 60 G | Verlangsamt Feinde auf 50 % |

### Feinde
- **Normale Feinde**: Skalieren pro Welle (HP: 25 + Welle×20, Geschwindigkeit: 35 + Welle×5)
- **Bosse**: Erscheinen auf Wellen 5, 10, 15 – hohe HP, langsamer, 60 Gold Belohnung

### Wellen-System
- 15 Wellen insgesamt
- 4 + Welle×2 Feinde pro Welle
- Wellenabschlussbonus: 30 Gold

### Ressourcen
- Startgold: 150 | Startleben: 20
- Gegner-Kill: 10 Gold / 25 Punkte
- Boss-Kill: 60 Gold / 200 Punkte

### Karte
- 20×12 Gitter, fixer Pfad mit 8 Wegpunkten
- Feinde folgen dem Pfad von links nach rechts

### UI & Sonstiges
- Hauptmenü, Highscore-Anzeige, Einstellungen-Platzhalter
- Deutsch als Spielsprache
- Highscores werden in-memory gespeichert (gehen beim Schließen verloren)

---

## Geplante Erweiterungen

Die folgenden Features sind priorisiert nach Aufwand und Spielwert:

### Priorität 1 – Kurzfristig (niedriger Aufwand)

#### Highscore-Persistenz
- Highscores plattformübergreifend dauerhaft speichern
- JVM/Desktop: Datei in `~/.towerdefense/`
- Android: SharedPreferences
- Web (JS/WASM): localStorage
- iOS: NSUserDefaults
- Serialisierungsformat: einfaches CSV pro Eintrag

#### Spielgeschwindigkeit (1× / 2×)
- Toggle-Button im Spiel-HUD
- Multipliziert die Delta-Zeit im Game-Loop
- Kein Einfluss auf Spielbalance, nur Komfort

#### Schwierigkeitsgrad (Einstellungen)
| Schwierigkeit | Startgold | Startleben | Feind-HP-Multiplikator |
|---------------|-----------|------------|------------------------|
| Einfach | 200 | 30 | × 0,75 |
| Normal | 150 | 20 | × 1,00 |
| Schwer | 100 | 10 | × 1,50 |

---

### Priorität 2 – Mittelfristig

#### Turm-Upgrades
- Türme nach dem Platzieren anklicken → Upgrade-Panel
- 3 Stufen (Basis, Stufe 2, Stufe 3)
- Kosten pro Stufe: Turmpreis × Stufe
- Stats pro Stufe: +50 % Schaden, +15 % Reichweite, −10 % Feuerrate

| Stufe | Upgrade-Kosten (Beispiel ROT) | Schaden | Reichweite | Feuerrate |
|-------|-------------------------------|---------|------------|-----------|
| 1 (Basis) | – | 55 | 144 px | 1,2 s |
| 2 | 75 G | 82 | 166 px | 1,1 s |
| 3 | 150 G | 110 | 187 px | 1,0 s |

#### Neue Feindtypen
| Typ | Geschwindigkeit | HP | Besonderheit |
|-----|-----------------|----|--------------|
| Schnell | × 2,0 | × 0,4 | Erscheint ab Welle 3 |
| Gepanzert | × 0,6 | × 2,5 | 10 Schadensreduktion, ab Welle 6 |
| Heiler | × 0,8 | × 1,2 | Regeneriert sich und Nachbarn, ab Welle 9 |

#### Zweite Karte (Wüste)
- Anderer Pfadverlauf: langer S-Bogen über die gesamte Karte
- Mehr Kurven → andere Turmplatzierungsstrategie
- Kartenauswahl vor dem Spielstart

---

### Priorität 3 – Langfristig

#### Wirtschaftssystem
- Türme verkaufen (50 % Rückerstattung)
- Zinssystem: Unausgegebenes Gold am Wellenende bringt +5 % Bonus

#### Fähigkeitssystem
- 3 Spezialangriffe mit Cooldown (z. B. Blitzschlag, Eisfeld, Gold-Regen)
- Nicht kaufbar, werden als Belohnung freigeschaltet

#### Endlosmodus
- Wellen gehen unbegrenzt weiter mit wachsender Schwierigkeit
- Separater Highscore-Eintrag

#### Sound & Musik
- Hintergrundmusik (loop)
- Soundeffekte: Schüsse, Explosionen, Boss-Erscheinen, Feind-Entkommen

#### Multiplayer / Koop
- Zwei Spieler teilen sich eine Karte kooperativ
- Oder kompetitiv: Wer hält länger?

---

## Technologie

This is a Kotlin Multiplatform project targeting Android, iOS, Web, Desktop (JVM).

* [/composeApp](./composeApp/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
    - [commonMain](./composeApp/src/commonMain/kotlin) is for code that’s common for all targets.
    - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
      For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
      the [iosMain](./composeApp/src/iosMain/kotlin) folder would be the right place for such calls.
      Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./composeApp/src/jvmMain/kotlin)
      folder is the appropriate location.

* [/iosApp](./iosApp/iosApp) contains iOS applications. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

### Build and Run Android Application

To build and run the development version of the Android app, use the run configuration from the run widget
in your IDE’s toolbar or build it directly from the terminal:

- on macOS/Linux
  ```shell
  ./gradlew :composeApp:assembleDebug
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:assembleDebug
  ```

### Build and Run Desktop (JVM) Application

To build and run the development version of the desktop app, use the run configuration from the run widget
in your IDE’s toolbar or run it directly from the terminal:

- on macOS/Linux
  ```shell
  ./gradlew :composeApp:run
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:run
  ```

### Build and Run Web Application

To build and run the development version of the web app, use the run configuration from the run widget
in your IDE's toolbar or run it directly from the terminal:

- for the Wasm target (faster, modern browsers):
    - on macOS/Linux
      ```shell
      ./gradlew :composeApp:wasmJsBrowserDevelopmentRun
      ```
    - on Windows
      ```shell
      .\gradlew.bat :composeApp:wasmJsBrowserDevelopmentRun
      ```
- for the JS target (slower, supports older browsers):
    - on macOS/Linux
      ```shell
      ./gradlew :composeApp:jsBrowserDevelopmentRun
      ```
    - on Windows
      ```shell
      .\gradlew.bat :composeApp:jsBrowserDevelopmentRun
      ```

### Build and Run iOS Application

To build and run the development version of the iOS app, use the run configuration from the run widget
in your IDE’s toolbar or open the [/iosApp](./iosApp) directory in Xcode and run it from there.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html),
[Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform),
[Kotlin/Wasm](https://kotl.in/wasm/)…

We would appreciate your feedback on Compose/Web and Kotlin/Wasm in the public Slack
channel [#compose-web](https://slack-chats.kotlinlang.org/c/compose-web).
If you face any issues, please report them on [YouTrack](https://youtrack.jetbrains.com/newIssue?project=CMP).