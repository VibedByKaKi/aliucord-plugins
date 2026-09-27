# Patching

Patches let you run code before, after, or instead of Discord methods. Under the hood Aliucord uses [Aliuhook](https://github.com/Aliucord/Hook) / LSPlant.

Upstream: `docs/documentation/plugin-dev/3_patching.md`  
Template patches: `docs/plugins-template/plugins/MyFirstKotlinPlugin/.../MyFirstKotlinPlugin.kt`  
Tiny real patch: `examples/Juby210-Aliucord-plugins/NoAutoReplyMention/`

## Basics

Every plugin has `patcher`. Prefer the Kotlin helpers when you can:

```kotlin
patcher.before<SomeClass>("methodName", ArgType::class.java) { param ->
    // runs before the original; set param.result to skip it
}

patcher.after<SomeClass>("methodName", ArgType::class.java) { param ->
    // runs after; param.result is the return value
}

patcher.instead<SomeClass>("methodName", ArgType::class.java) {
    // replaces the method; return the substitute result
    null
}
```

Java equivalents use `PreHook`, `Hook`, and `InsteadHook`. Prefer those wrappers over raw `XC_MethodHook` so Aliucord can catch and log failures.

You must pass parameter types so overloads resolve. Primitives use `Int::class.javaPrimitiveType` / `int.class`, not the boxed type, when the method takes a primitive.

## What `param` gives you

| Field / method | Before | After |
| --- | --- | --- |
| `thisObject` / typed `this` in Kotlin helpers | Instance (or null if static) | same |
| `args` | Mutable. Changes feed the original call | Original args |
| `result` / `setResult` | Setting it skips the original | Changes what callers see |
| `throwable` | — | Exception from the original, if any |

## Patterns that show up everywhere

### Replace a return value

```kotlin
patcher.instead<CoreUser>("getUsername") { "Clyde" }
```

### Tweak after the fact

```kotlin
patcher.after<CoreUser>("getUsername") {
    val name = it.result as String?
    if (name.equals("Clyde", ignoreCase = true)) it.result = "Evil Clyde"
}
```

### Mutate arguments before the call

`NoAutoReplyMention` forces Discord not to auto-mention on reply:

```java
patcher.patch(
    "com.discord.stores.StorePendingReplies", "onCreatePendingReply",
    new Class<?>[]{ Channel.class, Message.class, boolean.class, boolean.class },
    new PreHook(param -> {
        param.args[2] = false; // mention
        param.args[3] = true;  // showMentionToggle
    })
);
```

### No-op a method

```kotlin
patcher.instead<StoreUserTyping>("setUserTyping", Long::class.java) { null }
```

The template does this to hide typing. Same idea as Vendicated's quieter privacy-style hooks.

### Patch UI configure methods

The template's message stats embed patches `WidgetChatListAdapterItemMessage.onConfigure`. Guard against loading messages and against running twice on edits. That class of bug is easy to miss.

## Cleanup

```kotlin
override fun stop(context: Context) {
    patcher.unpatchAll()
}
```

Individual patches also return a `Runnable` you can call to remove just that hook.

## Finding what to patch

You cannot invent class names. Decompile Discord and search. See [Discord code and reflection](06-discord-and-reflection.md).

Next: [Settings](05-settings.md)
