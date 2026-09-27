# Discord code and reflection

Most interesting plugins patch Discord internals. You need to find the right class and method, then sometimes reach private members.

Upstream:

- `docs/documentation/plugin-dev/6_finding_discord_stuff.md`
- `docs/documentation/plugin-dev/5_reflection.md`

## Decompile Discord

Pull the APK Aliucord uses (legacy Discord **126.21**) and run JADX:

```bash
jadx --export-gradle --show-bad-code --fs-case-sensitive \
  --respect-bytecode-access-modifiers \
  --no-inline-methods --no-inline-anonymous --no-inline-kotlin-lambda \
  --output-dir decompiled base.apk
```

With [Aliucord's JADX fork](https://github.com/Aliucord/jadx) you can also pass `--no-generate-kotlin-metadata` to drop noisy `@Metadata` annotations.

A public decompile dump exists at [Juby210/discord-jadx](https://gitdab.com/Juby210/discord-jadx). It can be outdated. Prefer a fresh decompile of 126.21 when a symbol is missing.

For live UI ids while Aliucord is running, [Developer Assistant](https://play.google.com/store/apps/details?id=com.appsisle.developerassistant) helps.

## Search strategy that works

1. Reproduce the UI action in Discord.
2. Guess a class name (`Widget*`, `Store*`, `Model*`) and search the decompile.
3. Confirm with nearby strings, resource ids, or call stacks from other plugins.
4. Copy how a similar feature is patched in `examples/`.

Examples of good "find a peer and copy the target" plugins:

| Goal | Look at |
| --- | --- |
| Chat list / message UI | Template message embed patch; Vendicated `MessageLinkEmbeds`, `ShowBlockedMessages` |
| Stores / pending reply | Juby210 `NoAutoReplyMention` |
| User model fields | Template `CoreUser.getUsername` patches |
| Typing | Template `StoreUserTyping` instead-hook |

## Prefer access$ methods over reflection

Kotlin/Java compilers emit `access$...` bridges for private members. Call those first. They are faster and stabler than raw reflection.

```kotlin
WidgetMedia.`access$handlePlayerEvent`(widgetMedia, event)
```

## Reflection when you must

```kotlin
val field = ClassName::class.java.getDeclaredField("fieldName").apply {
    isAccessible = true
}
val value = field.get(instance) // null instance for static
```

```kotlin
val method = ClassName::class.java.getDeclaredMethod("methodName", Arg::class.java).apply {
    isAccessible = true
}
val result = method.invoke(instance, arg)
```

Same idea in Java with `setAccessible(true)`.

Cache reflected `Field`/`Method` objects. Looking them up on every call is slow.

## Obfuscation helpers

Some Discord getters are mangled. Aliucord ships wrappers/extensions for common ones (e.g. `MessageEmbedWrapper.title` used in the template). Check `com.aliucord.wrappers` before writing your own reflective accessors.

Next: [Build, deploy, publish](07-build-deploy-publish.md)
