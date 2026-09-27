# Plugin anatomy

Every plugin is one class that extends `Plugin` and carries `@AliucordPlugin`.

Upstream: `docs/documentation/plugin-dev/1_introduction.md`  
Template sample: `docs/plugins-template/plugins/MyFirstKotlinPlugin/`

## Minimal Kotlin skeleton

```kotlin
package com.github.yourname

import android.content.Context
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin

@AliucordPlugin(requiresRestart = false)
@Suppress("unused")
class MyPlugin : Plugin() {
    override fun start(context: Context) {
        // register commands / patches here
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
        commands.unregisterAll()
    }
}
```

`requiresRestart` tells Aliucord whether installing or updating the plugin needs an app restart.

## Lifecycle

Order is always:

`load` → `start` → `stop` → `unload`

| Method | When | Typical use |
| --- | --- | --- |
| `load(Context)` | Plugin is loaded | One-time init |
| `start(Context)` | Plugin is enabled | Register commands and patches |
| `stop(Context)` | Plugin is disabled | Unregister everything |
| `unload(Context)` | Plugin is unloaded | Tear down leftover state |

If `load` or `start` throws, Aliucord logs it and unloads the plugin.

## Built-in APIs on `Plugin`

| Field | Role |
| --- | --- |
| `commands` | `CommandsAPI` — slash commands |
| `patcher` | `PatcherAPI` — method hooks |
| `settings` | `SettingsAPI` — persisted key/value store |
| `settingsTab` | Optional settings UI entry on the plugin card |

Set `settingsTab` in an `init` block / constructor if you need a settings screen. See [Settings](05-settings.md).

## Monorepo layout

One GitHub repo usually holds many plugins. Each plugin is its own Gradle subproject:

```text
plugins/
  MyPlugin/
    build.gradle.kts
    src/main/kotlin/.../MyPlugin.kt
  AnotherPlugin/
    ...
```

That matches how almost every repo under `examples/` is structured.

## Cleanup habit

In `stop`, always undo what `start` did:

```kotlin
override fun stop(context: Context) {
    patcher.unpatchAll()
    commands.unregisterAll()
}
```

Leaving patches live after disable is a common crash source. Real plugins like Juby210's `NoAutoReplyMention` do exactly this pattern: patch in `start`, `unpatchAll` in `stop`.

Next: [Commands](03-commands.md)
