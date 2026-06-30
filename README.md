# Kaze

A minimal, fast Android browser. Native Kotlin + Jetpack Compose, no bloat.

## Highlights
- **WebView shell** with `shouldInterceptRequest` ad blocking (EasyList-style rules in `app/src/main/assets/adblock.txt`).
- **Max 4 tabs**, each its own lazily-created `WebView`. Inactive tabs are `onPause()`d; the active one is `onResume()`d.
- **Private mode** — no history saved, session cookies cleared on exit, distinct dark-purple UI.
- **Settings** — search engine (DuckDuckGo default, Brave, Google, Bing), ad-block toggle, dark/light toggle, clear history.
- **History** — date-grouped, swipe-to-delete (Room/SQLite, nothing synced).
- **Downloads** — system `DownloadManager`, Material file-type icons.
- **Overflow menu** — ad blocker, desktop mode, share, translate.

## Design
The UI matches the source design in `claudedesign/Kaze Browser.dc.html`
(palettes, layout, the "kaze" wind logo). Dark background is `#1E1E1E`.

## Tech / performance choices
- Only the dependencies actually used (Compose, Lifecycle, Room). No image loader,
  no navigation library, no analytics/telemetry/crash reporting.
- Icons are local vector drawables tinted per theme — no icon library.
- `minSdk 26` so the launcher icon is a single adaptive vector (no legacy PNGs).
- R8/resource shrinking enabled for `release`.

## Build
Requires **JDK 17** and the Android SDK (compileSdk 35).

```bash
./gradlew assembleDebug        # APK -> app/build/outputs/apk/debug/app-debug.apk
./gradlew test                 # unit tests for the ad-block matcher + URL resolver
```

Every push to `main` builds the debug APK via GitHub Actions
(`.github/workflows/build.yml`) and uploads it as the `kaze-debug` artifact.

## Notes / known limits
- `adblock.txt` is a curated EasyList subset; drop in the full EasyList to widen coverage.
- Private mode isn't a fully isolated WebView profile — it skips history and clears
  session cookies, but shares the global cookie/cache store (no extra dependency).
