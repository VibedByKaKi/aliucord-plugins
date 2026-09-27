# Setup

## What you need

- A PC (mobile-only development is unsupported)
- Roughly 8 GB RAM minimum, 16 GB is more comfortable on Windows
- [Git](https://git-scm.com/)
- [Android Studio](https://developer.android.com/studio) (recommended over VS Code)
- JDK 21+ (Adoptium / Temurin / Azul Zulu all fine)
- [JADX](https://github.com/skylot/jadx) or [Aliucord's JADX fork](https://github.com/Aliucord/jadx) for decompiling Discord
- A physical Android device or x86_64 emulator with Aliucord installed via [Aliucord Manager](https://github.com/Aliucord/Manager/releases/latest)

Older notes in `docs/documentation/plugin-dev/0_prerequisites.md` still mention JDK 11. The current template and gradle plugin target JDK 21. Follow the template.

If Gradle fails TLS handshakes on older JDKs, add this to `gradle.properties`:

```properties
org.gradle.jvmargs=-Dhttps.protocols=TLSv1.2
```

## Create your plugin repo

Use the official template. Local copy lives at `docs/plugins-template`. Upstream: [Aliucord/plugins-template](https://github.com/Aliucord/plugins-template).

1. On GitHub: **Use this template** → create a new repo.
2. Tick **Include all branches**. That brings the empty `builds` branch you need for publishing later.
3. Clone your new repo.
4. Open it in Android Studio.

## Fill the TODOs

Open `plugins/build.gradle.kts` and set:

- `namespace` to your package (e.g. `com.github.yourname`)
- `author("yourname", discordUserId, hyperlink = true)`
- `github("https://github.com/you/your-plugins")`

Per-plugin metadata lives in each plugin's own `build.gradle.kts`:

```kotlin
version = "1.0.0"
description = "What the plugin does"

aliucord {
    changelog.set(
        """
        # 1.0.0
        * Initial release
        """.trimIndent(),
    )
    deploy.set(false) // flip to true when you are ready to publish
}
```

## Android Studio bits that matter

1. Settings → Experimental → enable **Configure all Gradle tasks during Gradle sync**.
2. Sync Gradle (`Ctrl+Shift+O` / `Cmd+Shift+O`).
3. Open the Gradle tool window. Under your plugin project you should see `deployWithAdb` and `make`.

## Device prep

1. Install Aliucord with Manager.
2. Sign in with a **throwaway** Discord account. Do not test on your main.
3. Plug in the phone (or start the emulator) and accept the ADB authorization prompt.

## First build

From Android Studio, run the plugin's `deployWithAdb` task.

From the CLI:

```bash
# Linux / macOS
./gradlew :MyFirstKotlinPlugin:make
./gradlew :MyFirstKotlinPlugin:deployWithAdb

# Windows
.\gradlew.bat :MyFirstKotlinPlugin:make
.\gradlew.bat :MyFirstKotlinPlugin:deployWithAdb
```

Built zips land under `$PLUGIN_DIR/build/outputs/` (path printed by the `make` task). The `build` directories are often hidden in Android Studio's tree view.

## Gradle plugin context

`docs/gradle-plugin` documents the build tooling. Plugins currently compile against Discord **126.21** (`126021`). Discord and Aliucord are `compileOnly` dependencies. You do not ship them inside the zip.

Next: [Plugin anatomy](02-plugin-anatomy.md)
