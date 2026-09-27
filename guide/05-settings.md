# Settings

Use `settings` (`SettingsAPI`) to persist config. Keys are auto-prefixed with your plugin name.

Upstream: `docs/documentation/plugin-dev/4_settings.md`  
Real settings page: `examples/Vendicated-AliucordPlugins/Hastebin/.../PluginSettings.java`

## Storage

```kotlin
settings.setString("mirror", "https://haste.example")
val mirror = settings.getString("mirror", "https://haste.powercord.dev")

settings.setBool("enabled", true)
settings.getBool("enabled", false)

settings.setInt("limit", 50)
// Objects are JSON-stringified
```

Files live at `/Aliucord/settings/[PluginName].json` on the device.

Keep this small. Big blobs and binary data belong in cache dirs, not settings.

## Attach a settings UI

Set `settingsTab` in the constructor / `init` so the plugin card shows a gear:

```kotlin
class MyPlugin : Plugin() {
    init {
        settingsTab = SettingsTab(MySettingsPage::class.java).withArgs(settings)
    }
}
```

```java
public MyPlugin() {
    settingsTab = new SettingsTab(PluginSettings.class).withArgs(settings);
}
```

`withArgs` passes constructor arguments into your page class. Passing `settings` is the usual pattern.

## Dedicated page vs bottomsheet

| Kind | Base class | Override |
| --- | --- | --- |
| Full page | `com.aliucord.fragments.SettingsPage` | `onViewBound(View)` |
| Bottomsheet | `com.aliucord.widgets.BottomSheet` | `onViewCreated(View, Bundle)` |

Both expose `addView(...)`. Prefer Aliucord widgets (`TextInput`, `Button`, etc.) so the UI matches Discord's theme.

## Minimal page sketch

```java
public final class PluginSettings extends SettingsPage {
    private final SettingsAPI settings;

    public PluginSettings(SettingsAPI settings) {
        this.settings = settings;
    }

    @Override
    public void onViewBound(View view) {
        super.onViewBound(view);
        setActionBarTitle("My Plugin");

        var input = new TextInput(requireContext());
        input.setHint("Some option");
        input.getEditText().setText(settings.getString("option", "default"));

        var button = new Button(requireContext());
        button.setText("Save");
        button.setOnClickListener(v -> {
            settings.setString("option", input.getEditText().getText().toString());
            Utils.showToast("Saved!");
            close();
        });

        addView(input);
        addView(button);
    }
}
```

Hastebin's settings page validates a URL before enabling Save. Worth reading if you need input validation.

For bottomsheet-style toggles, look at smaller Vendicated plugins like `EmojiReplacer` / `TextFilePreview` under `examples/Vendicated-AliucordPlugins/`.

Next: [Discord code and reflection](06-discord-and-reflection.md)
