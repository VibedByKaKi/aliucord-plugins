# Commands

Aliucord hooks Discord slash commands. Anything you register shows up next to the real ones.

Upstream: `docs/documentation/plugin-dev/2_commands.md`  
Template demo: `docs/plugins-template/plugins/MyFirstKotlinPlugin/.../MyFirstKotlinPlugin.kt`  
Real example: `examples/Vendicated-AliucordPlugins/Hastebin/`

## Register and unregister

```kotlin
commands.registerCommand("hello", "My first command!") {
    CommandsAPI.CommandResult("Hello World!")
}
```

With options:

```kotlin
commands.registerCommand(
    "hellowitharguments",
    "Hello with args",
    listOf(
        Utils.createCommandOption(ApplicationCommandType.STRING, "name", "Person to greet"),
        Utils.createCommandOption(ApplicationCommandType.USER, "user", "User to greet"),
    ),
) { ctx ->
    val username = if (ctx.containsArg("user")) {
        ctx.getRequiredUser("user").username
    } else {
        ctx.getStringOrDefault("name", "World")
    }
    CommandsAPI.CommandResult("Hello $username!")
}
```

Unregister with `commands.unregisterCommand("hello")` or `commands.unregisterAll()` in `stop`.

Pick unique command names. Collisions with other plugins are painful for users.

## Callback details

- Runs on a background thread. HTTP and other heavy work are fine here.
- Receives a `CommandContext`. See [CommandContext](https://aliucord.github.io/dokka/html/-aliucord/com.aliucord.entities/-command-context).
- Returns a `CommandsAPI.CommandResult`, or `null` for no reply.
- Errors are caught and shown to the user, but still write defensive code.

`CommandResult` constructor shape in practice:

```kotlin
CommandsAPI.CommandResult(
    content,   // message text
    embeds,    // List of embeds, or null
    send,      // true = visible to everyone, false = ephemeral-style bot reply
)
```

## Options

Build options with `Utils.createCommandOption(...)`. Do not construct `CommandChoice` yourself. Use `Utils.createCommandChoice`.

Common types: `STRING`, `INTEGER`, `BOOLEAN`, `USER`, `CHANNEL`, `ROLE`, and subcommand variants.

Hastebin wires a required string plus an optional boolean, then posts to a mirror URL from settings:

```java
// examples/Vendicated-AliucordPlugins/Hastebin/.../Hastebin.java
commands.registerCommand("haste", "Create pastes on hastebin", arguments, ctx -> {
    var text = ctx.getRequiredString("text");
    var send = ctx.getBoolOrDefault("send", false);
    // Http.simpleJsonPost(...), then:
    return new CommandsAPI.CommandResult(result, null, send);
});
```

That pattern (command + settings + HTTP) is a good first "real" plugin to copy.

## When not to use commands

If the feature is about changing Discord UI or network behavior, you want a [patch](04-patching.md), not a slash command. Many popular plugins never register a command at all.

Next: [Patching](04-patching.md)
