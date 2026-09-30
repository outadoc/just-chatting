#!/usr/bin/env bash
#
# Regenerates the landing page screenshots from the Compose screenshot tests in
# app-android/src/screenshotTest/.../landing, and copies them to the website.
#
# Usage: scripts/update-landing-screenshots.sh [--skip-gradle]

set -euo pipefail

cd "$(dirname "$0")/.."

if [[ "${1:-}" != "--skip-gradle" ]]; then
    ./gradlew :app-android:updateDebugScreenshotTest
fi

src="app-android/src/screenshotTestDebug/reference/fr/outadoc/justchatting/landing/LandingScreenshotTestKt"
dest="docs/assets/screenshots/landing"

mkdir -p "$dest"

# LandingDynamicColorsPokeScreenshotTest_dark_65fc2005_0.png -> dynamic-colors-poke-dark.png
for file in "$src"/Landing*ScreenshotTest_*_0.png; do
    base="$(basename "$file")"
    name="${base#Landing}"
    name="${name%%ScreenshotTest_*}"
    theme="${base#*ScreenshotTest_}"
    theme="${theme%%_*}"
    slug="$(echo "$name" | sed -E 's/([a-z0-9])([A-Z])/\1-\2/g' | tr '[:upper:]' '[:lower:]')"
    cp "$file" "$dest/$slug-$theme.png"
done

echo "Copied $(ls "$dest" | wc -l) screenshots to $dest"
