# multi-select-copy

Copy a contiguous range of chat messages on Aliucord Android.

## Install

1. Download `multi-select-copy.zip` from this repo's [`builds`](https://github.com/VibedByKaKi/aliucord-plugins/tree/builds) branch, or from the latest GitHub Actions artifact named `plugins`.
2. Move the zip into `Aliucord/plugins` on your device (or use PluginDownloader once the repo is listed).
3. Restart Aliucord if needed.

## Usage

1. Long-press the first message → **Copy from here**.
2. Long-press the last message → **Copy through here** (only appears after step 1).
3. Paste anywhere.

Choosing **Copy from here** again replaces a mistaken start. Choosing **Copy through here** on the same message copies just that one.

Design notes: [UX.md](UX.md)

## Build

From the repo root (JDK 21 + Android SDK):

```bash
./gradlew :plugins:multi-select-copy:make
```

ZIP output: `plugins/multi-select-copy/build/outputs/multi-select-copy.zip`
