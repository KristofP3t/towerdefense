#!/bin/zsh
# Baut die APK und installiert sie direkt auf dem verbundenen Android-Gerät.
# Funktioniert per USB und per WLAN (nach einmaliger Einrichtung mit adb tcpip 5555).
#
# Verwendung:
#   ./deploy-android.sh
#
# Einmaliges WLAN-Setup (nach erstem USB-Start):
#   ~/Library/Android/sdk/platform-tools/adb tcpip 5555
#   ~/Library/Android/sdk/platform-tools/adb connect <TABLET-IP>:5555

ADB="$HOME/Library/Android/sdk/platform-tools/adb"

echo "📦 Baue APK …"
./gradlew :composeApp:assembleDebug || { echo "❌ Build fehlgeschlagen"; exit 1; }

echo "🔍 Suche verbundenes Gerät …"
DEVICE=$("$ADB" devices | awk 'NR>1 && /device$/ {print $1; exit}')

if [ -z "$DEVICE" ]; then
    echo "❌ Kein Gerät gefunden."
    echo "   → USB: Kabel anschließen und USB-Debugging auf dem Tablet erlauben."
    echo "   → WLAN: '$ADB connect <TABLET-IP>:5555' ausführen."
    exit 1
fi

echo "📲 Installiere auf $DEVICE …"
"$ADB" -s "$DEVICE" install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk \
    && echo "✅ Erfolgreich installiert! App startet automatisch …" \
    && "$ADB" -s "$DEVICE" shell monkey -p com.example.towerdefense 1 > /dev/null 2>&1 \
    || echo "❌ Installation fehlgeschlagen"
