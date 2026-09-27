# Aliucord plugin guide

How to build plugins for [Aliucord](https://github.com/Aliucord/Aliucord), the legacy Android Discord client mod.

This guide is distilled from the upstream material in `docs/` and real plugins in `examples/`. Prefer Kotlin for new work. Java still works.

## Contents

1. [Setup](01-setup.md) — tools, template repo, first Gradle sync
2. [Plugin anatomy](02-plugin-anatomy.md) — class, annotation, lifecycle
3. [Commands](03-commands.md) — slash commands via `CommandsAPI`
4. [Patching](04-patching.md) — hook Discord methods with `PatcherAPI`
5. [Settings](05-settings.md) — persist config and add a settings UI
6. [Discord code and reflection](06-discord-and-reflection.md) — find targets, reach private members
7. [Build, deploy, publish](07-build-deploy-publish.md) — zip, ADB, `builds` branch, review
8. [Learning from examples](08-learning-from-examples.md) — which cloned repos to read for what

## Upstream sources

| Local path | What it is |
| --- | --- |
| `docs/documentation` | Official plugin/theme/smali docs |
| `docs/plugins-template` | Official monorepo template with sample plugins |
| `docs/gradle-plugin` | Gradle plugin that packages and deploys plugins |
| `examples/*` | Distributed community and official plugin repos |

API reference: [Aliucord Dokka](https://aliucord.github.io/dokka/html/-aliucord/index.html)

## Quick path

1. Fork/use `docs/plugins-template` (or clone the GitHub template with all branches).
2. Fill the `// TODO` placeholders in `plugins/build.gradle.kts`.
3. Copy `MyFirstKotlinPlugin`, rename it, write your `start`/`stop`.
4. Run `deployWithAdb` against a test account on Aliucord.
5. Read a small real plugin from `examples/` when something is unclear.
