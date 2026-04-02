#!/bin/zsh
# Baut die WasmJS-Distribution und deployt sie auf den gh-pages-Branch.
#
# Verwendung:
#   ./deploy-web.sh

set -e

DIST="composeApp/build/dist/wasmJs/productionExecutable"

echo "📦 Baue WasmJS-Distribution …"
./gradlew :composeApp:wasmJsBrowserDistribution

echo "🌿 Wechsle auf gh-pages-Branch …"
git fetch origin
if git show-ref --quiet refs/remotes/origin/gh-pages; then
    git worktree add /tmp/gh-pages-deploy origin/gh-pages
else
    # Branch existiert noch nicht → leeren Branch anlegen
    git worktree add --orphan -b gh-pages /tmp/gh-pages-deploy
fi

echo "🗑  Alte Dateien löschen …"
rm -rf /tmp/gh-pages-deploy/*

echo "📋 Neue Dateien kopieren …"
cp -r "$DIST"/. /tmp/gh-pages-deploy/

echo "📤 Pushen …"
cd /tmp/gh-pages-deploy
git add -A
git commit -m "deploy: $(date '+%Y-%m-%d %H:%M')" || echo "Keine Änderungen."
git push origin gh-pages

echo "🧹 Aufräumen …"
cd -
git worktree remove /tmp/gh-pages-deploy --force

echo "✅ Fertig! App verfügbar unter: https://kristofp3t.github.io/towerdefense/"
