#!/bin/zsh
# Baut die WasmJS-Distribution und deployt sie auf den gh-pages-Branch.
#
# Verwendung:
#   ./deploy-web.sh

set -e

DIST="composeApp/build/dist/wasmJs/productionExecutable"
REMOTE=$(git remote get-url origin)
TMPDIR="/tmp/gh-pages-deploy-$$"

echo "📦 Baue WasmJS-Distribution …"
./gradlew :composeApp:wasmJsBrowserDistribution

echo "📋 Dateien in temporäres Verzeichnis kopieren …"
rm -rf "$TMPDIR"
mkdir "$TMPDIR"
cp -r "$DIST"/. "$TMPDIR/"

echo "🌿 gh-pages-Branch vorbereiten …"
cd "$TMPDIR"
git init -q
git checkout -q -b gh-pages
git add -A
git commit -q -m "deploy: $(date '+%Y-%m-%d %H:%M')"

echo "📤 Pushen nach $REMOTE …"
git push --force "$REMOTE" gh-pages

echo "🧹 Aufräumen …"
cd -
rm -rf "$TMPDIR"

echo "✅ Fertig! App verfügbar unter: https://kristofp3t.github.io/towerdefense/"
