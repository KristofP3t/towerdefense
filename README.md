# Tower Defense

Ein Tower-Defense-Spiel entwickelt mit Kotlin Multiplatform und Compose Multiplatform, verfügbar für Android, Web (WasmJS) und Desktop (JVM).

---

## Spielprinzip

Verteidige deine Basis gegen Wellen von Feinden, indem du Türme auf dem Spielfeld platzierst. Besiege alle 15 Wellen – inklusive Boss-Wellen alle 5 Runden – um zu gewinnen.

---

## Implementierte Features

### Hauptmenü & Navigation
- Hauptmenü mit Buttons: Neues Spiel, Highscores, Einstellungen
- Kartenauswahl vor dem Spielstart (5 Karten)
- Schwierigkeitsgrad wählbar (Einfach / Normal / Schwer)
- Statistik-Screen nach Spielende mit Namenseingabe für Highscores
- Highscore-Tabelle (persistent: Datei auf Desktop, SharedPreferences auf Android, localStorage im Browser)

### Türme (3 Typen, 3 Stufen)
| Typ    | Schaden | Reichweite | Feuerrate | Kosten | Besonderheit         |
|--------|---------|------------|-----------|--------|----------------------|
| ROT    | 55      | 144 px     | 1,2 s     | 75 ¢   | Hoher Schaden        |
| GELB   | 20      | 240 px     | 1,6 s     | 50 ¢   | Größte Reichweite    |
| BLAU   | 12      | 160 px     | 2,0 s     | 60 ¢   | Verlangsamt Feinde   |

- Türme anklicken → Upgrade-Panel (bis Stufe 3)
- Türme verkaufen (50 % Rückerstattung)
- Mündungsblitz-Animation beim Schuss
- Upgrade-Punkte als visuelle Anzeige auf dem Turm

### Feinde (3 Varianten)
| Typ       | HP-Mult | Speed-Mult | Rüstung | Ab Welle |
|-----------|---------|------------|---------|----------|
| Normal    | ×1,0    | ×1,0       | 0       | 1        |
| Schnell   | ×0,4    | ×2,0       | 0       | 3        |
| Gepanzert | ×2,5    | ×0,6       | 10      | 6        |
| Boss      | wave×120 | 22 px/s   | 0       | 5/10/15  |

- Bobbing-Animation beim Laufen
- Verlangsamungs-Ring (blauer Ring) bei verlangsamten Feinden
- Panzerungs-Ring bei gepanzerten Feinden
- Lebensbalken über jedem Feind

### Wellen-System
- 15 Wellen insgesamt
- 4 + Welle×2 normale Feinde pro Welle
- Alle 5 Wellen: Boss-Welle
- 30 ¢ Gold-Bonus nach jeder abgeschlossenen Welle

### Ressourcen & Schwierigkeit
| Schwierigkeit | Startgold | Startleben | Feind-HP-Mult |
|---------------|-----------|------------|---------------|
| Einfach       | 200 ¢     | 30         | ×0,75         |
| Normal        | 150 ¢     | 20         | ×1,00         |
| Schwer        | 100 ¢     | 10         | ×1,50         |

- Gegner-Kill: 10 ¢ / 25 Punkte
- Boss-Kill: 60 ¢ / 200 Punkte

### Karten (5 Stück)
| Karte    | Besonderheit              |
|----------|---------------------------|
| Wald     | Klassischer Pfad          |
| Wüste    | S-Kurven                  |
| Gebirge  | Enge Serpentinen          |
| Küste    | U-förmiger Pfad           |
| Vulkan   | Verzweigung (2 Äste)      |

- Auf der Vulkan-Karte wählen Feinde an der Gabelung zufällig einen von zwei Ästen
- Canvas-Mini-Vorschau pro Karte in der Kartenauswahl

### HUD & Steuerung
- **Pause-Button (⏸ / ▶)**: Spiel pausieren und fortsetzen
- **Geschwindigkeits-Toggle (1× / 2×)**: Spielgeschwindigkeit verdoppeln
- **Beenden-Button (✕ Menü)**: Spiel abbrechen und zu Statistiken wechseln
- Turm-Auswahlleiste unten mit Kostenanzeige (rot = zu teuer)
- Upgrade-Panel beim Antippen eines platzierten Turms

### Visuelles & Audio
- Partikel-System: Explosionen beim Feind-Tod (Farbe je Variante, Boss mit 14 Partikeln)
- Synthesize Audio: Schuss-, Kill- und Boss-Kill-Sounds (plattformspezifisch, keine Audiodateien nötig)
- Reichweiten-Ringe der Türme auf dem Spielfeld sichtbar
- Highlight der ausgewählten Gitterzelle

### Statistik-Screen
Nach Spielende werden angezeigt:
- Türme gebaut, Gegner getötet, Bosse getötet
- Schüsse abgefeuert, Trefferquote (%)
- Gold verdient
- Namenseingabe (max. 20 Zeichen) → Eintrag in Highscore-Tabelle

---

## Android-Deployment

Das Skript `deploy-android.sh` baut die Debug-APK und installiert sie direkt per ADB:

```bash
./deploy-android.sh
```

### Einmaliges WLAN-Setup (nach erstem USB-Start):
```bash
~/Library/Android/sdk/platform-tools/adb tcpip 5555
~/Library/Android/sdk/platform-tools/adb connect <TABLET-IP>:5555
```

Danach funktioniert das Deployment kabellos, solange Tablet und Mac im selben WLAN sind.

> **Hinweis für Xiaomi/HyperOS:** Bei der ersten Installation muss "Installation über USB" in den Entwickleroptionen aktiviert sein.

---

## Build & Run

### Android
```bash
./gradlew :composeApp:assembleDebug
# oder direkt deployen:
./deploy-android.sh
```

### Desktop (JVM)
```bash
./gradlew :composeApp:run
```

### Web (WasmJS)
```bash
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

### Web (JS, ältere Browser)
```bash
./gradlew :composeApp:jsBrowserDevelopmentRun
```

---

## Technologie

- **Kotlin Multiplatform** – gemeinsame Spiellogik für alle Plattformen
- **Compose Multiplatform** – deklaratives UI auf Android, Desktop und Web
- **Canvas-Rendering** – Spielfeld, Feinde, Türme, Partikel per `DrawScope`
- **`withFrameMillis`-Game-Loop** – Frame-genaues Update ohne externen Timer
- **`expect`/`actual`** – plattformspezifische Implementierungen für Storage und Audio
- **`mutableStateOf`** – reaktive Compose-State-Integration im GameEngine

Zielplattformen: Android · Desktop (JVM) · Web (WasmJS / JS)
