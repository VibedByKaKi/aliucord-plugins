# Build, deploy, publish

## Local build

```bash
./gradlew :MyPlugin:make
```

The task prints the zip path. Drop that zip into `/sdcard/Aliucord/plugins` on the device if you are installing by hand, then restart Aliucord.

## Deploy with ADB

```bash
./gradlew :MyPlugin:deployWithAdb
```

This builds, pushes, and restarts Aliucord. Fastest edit loop once ADB is authorized.

Requirements:

- Aliucord installed via Manager
- Device/emulator connected
- Gradle sync completed with "Configure all Gradle tasks during Gradle sync" enabled

## Version bumps

Increment `version` in the plugin's `build.gradle.kts` when you want clients to see an update. Update `changelog` at the same time. PluginDownloader and the updater JSON on the `builds` branch key off that metadata.

## Publishing pipeline

1. Ensure a `builds` branch exists (included if you cloned the template with all branches). If not:

   ```bash
   git stash
   git checkout --orphan builds
   git rm -rf .
   git commit --allow-empty -m "feat: init builds"
   git push -u origin builds
   git checkout main   # or master
   git stash pop
   ```

2. Set `deploy.set(true)` in the plugin's `aliucord { }` block.
3. Push `main`/`master`. GitHub Actions builds and writes zips + updater metadata to `builds`.
4. Optional but recommended: add a `GRADLE_CACHE_ENCRYPTION_KEY` Actions secret (`openssl rand -base64 16`) so CI uses the Gradle config cache.

## Getting listed

1. Join the [Aliucord Discord](https://discord.gg/EsNDvBaHVU).
2. Ask for a repo review in `#plugin-development`.
3. After approval, post to `#plugins-list` / `#new-plugins` as instructed.
4. Submit the repo to the internal listing via [aliucord/plugins-repo](https://github.com/aliucord/plugins-repo).

Users install either through PluginDownloader (long-press a listing message) or by downloading the zip from your `builds` branch.

## Gradle plugin notes

From `docs/gradle-plugin`:

- Discord artifact version is currently `126021` only.
- Apply `com.aliucord.plugin` on the root project and on each plugin subproject.
- Use `compileOnly` for Discord, Aliucord, and the Kotlin stdlib.

Stick to the template wiring unless you have a reason to hand-roll Gradle. Most breakage in new repos is a missed TODO or a missing `builds` branch.

Next: [Learning from examples](08-learning-from-examples.md)
