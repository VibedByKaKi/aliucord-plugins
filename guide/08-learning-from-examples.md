# Learning from examples

`examples/` holds widely distributed plugin monorepos cloned as submodules. Read those before reinventing a pattern.

## Start here

| Local path | Why |
| --- | --- |
| `docs/plugins-template/plugins/MyFirstKotlinPlugin` | Official tour of commands + patches in one file |
| `examples/Aliucord-plugins` | Official plugins repo / template sibling |
| `examples/Vendicated-AliucordPlugins` | High-signal plugins (Themer, PluginDownloader, TapTap, Hastebin) |
| `examples/Juby210-Aliucord-plugins` | Clean small patches (NoAutoReplyMention) and larger ones (MessageLogger) |
| `examples/rushiiMachine-aliucord-plugins` | Plugins from a core Aliucord maintainer |

## Pattern cheat sheet

| You want to learn… | Open |
| --- | --- |
| Slash command + HTTP + settings page | `examples/Vendicated-AliucordPlugins/Hastebin` |
| Argument-mutating PreHook | `examples/Juby210-Aliucord-plugins/NoAutoReplyMention` |
| Message list UI patch | Template `MyFirstKotlinPlugin`; Vendicated `MessageLinkEmbeds` |
| Settings bottomsheet / toggles | Vendicated `EmojiReplacer`, `TextFilePreview` |
| Complex theming / many patches | Vendicated `Themer` |
| Account / system utilities | `examples/zt64-aliucord-plugins` |
| Nitro spoof / layout control (older style) | `examples/X1nto-AliucordPlugins` |
| Backports and smaller utilities | `examples/mantikafasi-AliucordPlugins`, `examples/nyakowint-AliuPlugins` |
| Wing's plugins | `examples/wingio-plugins` |

## How to read a plugin repo

1. Skim the root `README` for the plugin list.
2. Open that plugin's `src/main/...` entry class annotated with `@AliucordPlugin`.
3. Jump to `start`. Commands and patches are almost always registered there.
4. Check for a `Settings` / `PluginSettings` class if `settingsTab` is set.
5. Compare its `build.gradle.kts` `aliucord { }` block with yours when publishing fails.

## Other clones worth browsing

Active or formerly popular collections also under `examples/`:

- `RhythmLunatic-aliucord-plugins`, `TymanWasTaken-aliucord-plugins`
- `c10udburst-aliucord-plugins`, `quincynyan-AliucordPlugins`, `peter1599-Aliucord-plugins`
- `swishs-aliucord-plugins`, `autodistries-aliucord-plugins`
- `lexisother-AliucordPlugins`, `nyxiereal-AliucordPlugins`, `Ushie-Aliucord-Plugins`
- Archived but still instructive: `oSumAtrIX-aliucord-plugins`, `scrazzz-AliucordPlugins`, `MrAn0nym-Aliucord-Plugins`
- Single-plugin repo: `js6pak-WhoReacted`

Some of these are unmaintained. Treat them as code samples, not as install targets, unless the `builds` branch is still what users ship.

## Upstream docs map

| Topic | Upstream file |
| --- | --- |
| Prerequisites | `docs/documentation/plugin-dev/0_prerequisites.md` |
| Introduction | `docs/documentation/plugin-dev/1_introduction.md` |
| Commands | `docs/documentation/plugin-dev/2_commands.md` |
| Patching | `docs/documentation/plugin-dev/3_patching.md` |
| Settings | `docs/documentation/plugin-dev/4_settings.md` |
| Reflection | `docs/documentation/plugin-dev/5_reflection.md` |
| Finding Discord code | `docs/documentation/plugin-dev/6_finding_discord_stuff.md` |
| Template README | `docs/plugins-template/README.md` |
| Gradle plugin | `docs/gradle-plugin/README.md` |

When this guide and upstream disagree on tooling versions, trust the template and gradle plugin READMEs. They move with the build stack.
