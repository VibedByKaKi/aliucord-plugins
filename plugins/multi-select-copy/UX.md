# Multi-select copy

Copy several consecutive chat messages in one shot on Aliucord Android.

## Problem

On desktop Discord you drag across messages, release, Ctrl-C. One gesture, free-form text.

On Android (and thus Aliucord) there is no cross-message selection. The loop is:

1. Long-press message
2. Tap Copy
3. Leave Discord / switch apps
4. Paste
5. Repeat for the next message

That breaks as soon as you need a contiguous chunk of conversation.

True desktop-style drag-select is a poor fit here. The chat list is a vertical `RecyclerView`: vertical drag already means scroll. Fighting that for text selection is fragile and fights every other gesture plugin (TapTap, etc.).

So the goal is not "recreate desktop selection". It is: **pick a start message, pick an end message, put the inclusive range on the clipboard**, with as few new UI surfaces as possible.

## Constraints from existing plugins

Patterns that already work in `examples/` and the docs:

| Pattern | Examples | Use for |
| --- | --- | --- |
| Add a row to the long-press sheet | `MessageLinkContext`, `ForwardMessages`, `FavoriteMessages` | Entry point |
| Patch message click | TapTap (`onMessageClicked`) | Optional second tap while selecting |
| Clipboard | `Utils.setClipboard(label, text)` (`HoldAuthorToCopyId`, FavoriteMessages) | Output |
| Message store lookup | `StoreStream.getMessages().getMessage(channelId, id)` | Resolve content by id |
| Skip bad messages | TapTap skips ephemeral / local / failed / loading | Reliability |

Stay inside those. No custom floating windows, no replacing the chat recycler adapter, no slash command as the primary path.

## Recommended UX: range anchors (v1)

Actions on the existing message long-press sheet (`WidgetChatListActions`). Which rows appear depends on whether a start is armed.

| Sheet state | Rows shown |
| --- | --- |
| No start armed | **Copy from here** only |
| Start armed (same channel) | **Copy from here** (re-arm) + **Copy through here** |

**Copy through here** must not appear until a start exists. That removes the dead-end "set a start first" toast and keeps the sheet minimal on normal long-presses.

### Flow

```text
Long-press message A  →  "Copy from here"
  → arm start = (channelId, messageId A)
  → toast: "Start set. Long-press the last message and choose Copy through here."
  → sheet dismisses

Long-press message A again (mistake)  →  "Copy from here"
  → replace start with A (same message is fine; just re-arms)
  → toast: "Start updated."
  → sheet dismisses

Long-press message C (different message, still a mistake)  →  "Copy from here"
  → replace start with C
  → toast: "Start updated."
  → sheet dismisses

Long-press message B  →  "Copy through here"   // only visible because start is armed
  → if other channel than start: toast "Start was in another channel", clear start, return
  → lo = min(startId, B), hi = max(startId, B)
  → if startId == B: range is that single message
  → collect loaded messages in channel with id in [lo, hi] inclusive
  → join text, Utils.setClipboard(...), toast "Copied N messages" (or "Copied 1 message")
  → clear start
```

Order of start vs end does not matter for multi-message ranges. Snowflake IDs are time-ordered.

### Copy from here twice (correcting a mistake)

**Copy from here** is always a set-or-replace, never a toggle and never an error.

- First tap: arm start, toast "Start set…"
- Any later tap (same message or another): overwrite start, toast "Start updated."

That way a wrong first pick is fixed by long-pressing the right message and choosing **Copy from here** again. No separate clear action.

### Same message as start and end

Choosing **Copy through here** on the same message that is already the start is valid. Treat it as a one-message copy of that message's content (same as Discord's built-in Copy for that bubble), then clear the start. Toast: `Copied 1 message`.

Useful when the user armed a start, then decided they only needed that one after all, without hunting for Discord's own Copy row.

### Why this wins for v1

- Reuses Discord's sheet. Same patch style as wingio / ForwardMessages (~one `configureUI` hook).
- Sheet stays clean: end action only shows when it can succeed.
- No selection mode chrome, no checkboxes, no sticky action bar.
- One piece of plugin state: optional start anchor.
- Mistakes are cheap: re-arm start in place.
- Does not steal single taps or double-taps from TapTap / reply / jump.

### What the user sees

Sheet items (icon + label, `UiKit_Settings_Item_Icon`, same as MessageLinkContext):

- Always when relevant: **Copy from here**
- Only after a start is armed in this channel: **Copy through here**

When **Copy through here** is shown, a one-line hint is fine, e.g. `Start: message from Alice`. No bottomsheet redesign.

Toasts only. No modal confirm for the happy path.

### Cancel

- Replacing via **Copy from here** on another (or the same) message, or
- Leaving the channel clears the start (recommended: clear on channel change).

## Alternative UX: selection mode (v1.5)

Closer to Gmail / gallery multi-select. Heavier.

### Flow

```text
Long-press message  →  "Select to copy"
  → enter selection mode
  → that message is selected
  → taps toggle messages in the same channel (or expand range to tapped message)
  → top or bottom bar: "Copy (N)" | "Cancel"
```

### Visuals

While selecting, patch `WidgetChatListAdapterItemMessage.onConfigure` (template already does this for embeds) to tint selected rows or draw a checkbox. That is the main reliability cost: view recycling, grouped messages, embeds, blocked messages.

### When to build this

Only if range anchors feel too hidden after trying v1. Selection mode is better for non-contiguous picks; range anchors only do contiguous. For "consecutive messages" (the stated problem), range is enough.

## Rejected approaches

| Idea | Why not |
| --- | --- |
| Drag across messages like desktop | Conflicts with scroll; hard to get right on a recycler |
| Double-tap starts selection | Collides with TapTap reply/edit |
| Slash command `/copyrange` | Awkward for browsing chat; fine as a debug escape hatch only |
| Replace Discord's single-message Copy | Surprising; breaks muscle memory |
| Persistent floating bubble | Extra surface, theme/z-order pain, not how examples work |

## Copy format

Default, keep it plain and paste-friendly:

```text
message one content

message two content

message three content
```

Blank line between messages. Skip empty content (stickers-only, etc.) or emit a one-line placeholder like `[sticker]` / `[attachment: name]` if present.

Optional settings (bottomsheet, Hastebin-style, off by default):

| Setting | Default | Effect |
| --- | --- | --- |
| Include author | off | `Alice: hello` |
| Include timestamp | off | Prefixed time string |
| Separator | blank line | Or `---` |
| Max messages | e.g. 100 | Cap runaway ranges; toast if truncated |

v1 can ship with content-only and a hard max. Settings come later.

## Edge cases

- **Start/end not both in memory.** Chat only keeps a window of loaded messages. Copy only what `StoreStream.getMessages()` has for that channel between the ids. Toast if the range looks sparse: `Copied 12 messages (some may be unloaded)`. Do not fetch history over HTTP in v1.
- **Same message as start and end.** Copy that one message, clear start. See above.
- **Copy from here twice.** Always replace start. Same message or different message, same behavior.
- **Copy through here with no start.** Should be unreachable in the UI (row hidden). If state races, no-op + clear.
- **Ephemeral / failed / local / loading.** Do not allow arming start on these; skip them in the collected range. Same idea as TapTap.
- **Channel switch / guild switch.** Clear armed start (hides **Copy through here** again).
- **Plugin stop / disable.** Clear state in `stop`.

## Implementation sketch (aligned with docs/examples)

Keep the plugin tiny. Rough shape:

```text
plugins/multi-select-copy/
  UX.md                 ← this file
  README.md             ← user-facing summary once coded
  (later) MultiSelectCopy.kt
  (later) build.gradle.kts
```

Code path for v1:

1. `patcher.after` / `Hook` on `WidgetChatListActions.configureUI` — always add **Copy from here**; add **Copy through here** only if `start != null` and `start.channelId == current message's channel`. Mirror MessageLinkContext / ForwardMessages / FavoriteMessages' conditional row.
2. In-memory `var start: Pair<Long, Long>?` = channelId + messageId.
3. On **Copy from here**: `start = (channelId, messageId)` (overwrite if already set); toast set vs updated.
4. On **Copy through here**: if `startId == endId`, copy that one message; else walk loaded messages with id in `[lo, hi]`; `Utils.setClipboard("Messages", text)`; toast; clear start.
5. `stop` → `patcher.unpatchAll()` + clear start.

No commands required. Optional later: a settings bottomsheet for format flags.

## Recommendation

Ship **range anchors** first. It matches the desktop *outcome* (contiguous chunk on the clipboard) without pretending Android has mouse-drag selection, and it stays inside the patch styles already proven in `examples/`.

Selection mode is a follow-up if people need cherry-picking or clearer affordances.
