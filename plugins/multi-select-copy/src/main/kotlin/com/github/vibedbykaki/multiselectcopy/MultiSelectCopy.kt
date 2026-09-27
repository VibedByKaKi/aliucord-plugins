package com.github.vibedbykaki.multiselectcopy

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.after
import com.aliucord.utils.ReflectUtils
import com.discord.models.message.Message
import com.discord.stores.StoreMessages
import com.discord.stores.StoreMessagesHolder
import com.discord.stores.StoreStream
import com.discord.utilities.color.ColorCompat
import com.discord.widgets.chat.list.actions.WidgetChatListActions
import com.lytefast.flexinput.R

/**
 * Long-press sheet actions to copy a contiguous range of loaded messages.
 *
 * See plugins/multi-select-copy/UX.md
 */
@AliucordPlugin(requiresRestart = false)
@Suppress("unused")
class MultiSelectCopy : Plugin() {
    private data class Anchor(val channelId: Long, val messageId: Long)

    private var start: Anchor? = null

    private lateinit var messagesHolder: StoreMessagesHolder

    private val fromHereId = View.generateViewId()
    private val throughHereId = View.generateViewId()
    private val clearStartId = View.generateViewId()

    override fun start(context: Context) {
        messagesHolder = ReflectUtils.getField(StoreStream.getMessages(), "holder") as StoreMessagesHolder

        // Drop a pending start when the user switches channels
        patcher.after<StoreMessages>("handleChannelSelected", Long::class.javaPrimitiveType!!) {
            clearStart(silent = true)
        }

        patcher.after<WidgetChatListActions>(
            "configureUI",
            WidgetChatListActions.Model::class.java,
        ) { param ->
            val sheet = param.thisObject as WidgetChatListActions
            val rootView = sheet.view as? NestedScrollView ?: return@after
            val layout = rootView.getChildAt(0) as? LinearLayout ?: return@after
            val model = param.args[0] as WidgetChatListActions.Model
            val message = model.message

            if (isUnusable(message)) return@after

            val armed = start
            if (armed != null && armed.channelId != message.channelId) {
                clearStart(silent = true)
            }

            if (layout.findViewById<View>(fromHereId) != null) return@after

            val ctx = layout.context
            val copyIcon = themedIcon(ctx)

            layout.addView(
                sheetItem(ctx, fromHereId, "Copy from here", copyIcon) {
                    val replacing = start != null
                    start = Anchor(message.channelId, message.id)
                    Utils.showToast(
                        if (replacing) {
                            "Start updated."
                        } else {
                            "Start set. Long-press the last message and choose Copy through here."
                        },
                    )
                    sheet.dismiss()
                },
            )

            val currentStart = start
            if (currentStart != null && currentStart.channelId == message.channelId) {
                layout.addView(
                    sheetItem(ctx, throughHereId, "Copy through here", copyIcon) {
                        copyRange(currentStart, message)
                        sheet.dismiss()
                    },
                )
                layout.addView(
                    sheetItem(ctx, clearStartId, "Clear copy start", copyIcon) {
                        clearStart(silent = false)
                        sheet.dismiss()
                    },
                )
            }
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
        clearStart(silent = true)
    }

    private fun copyRange(anchor: Anchor, end: Message) {
        if (anchor.channelId != end.channelId) {
            Utils.showToast("Start was in another channel")
            clearStart(silent = true)
            return
        }

        val lo = minOf(anchor.messageId, end.id)
        val hi = maxOf(anchor.messageId, end.id)

        val channelMessages = messagesHolder.getMessagesForChannel(anchor.channelId)
        if (channelMessages.isNullOrEmpty()) {
            Utils.showToast("No loaded messages to copy")
            clearStart(silent = true)
            return
        }

        val inRange = channelMessages.values
            .asSequence()
            .filter { it.id in lo..hi }
            .filterNot { isUnusable(it) }
            .sortedBy { it.id }
            .toList()

        if (inRange.isEmpty()) {
            Utils.showToast("No loaded messages in that range")
            clearStart(silent = true)
            return
        }

        val truncated = inRange.size > MAX_MESSAGES
        val toCopy = if (truncated) inRange.take(MAX_MESSAGES) else inRange
        val text = toCopy.joinToString("\n\n") { formatMessage(it) }.trim()

        if (text.isEmpty()) {
            Utils.showToast("Nothing to copy in that range")
            clearStart(silent = true)
            return
        }

        Utils.setClipboard("Messages", text)

        val n = toCopy.size
        val toast = buildString {
            append(if (n == 1) "Copied 1 message" else "Copied $n messages")
            if (truncated) append(" (capped at $MAX_MESSAGES)")
        }
        Utils.showToast(toast)
        clearStart(silent = true)
    }

    private fun formatMessage(message: Message): String {
        val content = message.content?.trim().orEmpty()
        if (content.isNotEmpty()) return content

        val attachmentCount = message.attachments.size
        if (attachmentCount > 0) {
            return if (attachmentCount == 1) "[attachment]" else "[$attachmentCount attachments]"
        }

        if (message.embeds.isNotEmpty()) return "[embed]"
        return "[empty message]"
    }

    private fun isUnusable(message: Message): Boolean =
        message.isEphemeralMessage ||
            message.isLocal ||
            message.isFailed ||
            message.isLoading

    private fun clearStart(silent: Boolean) {
        if (start == null) return
        start = null
        if (!silent) Utils.showToast("Copy start cleared")
    }

    private fun themedIcon(ctx: Context): Drawable? {
        val resId = listOf("ic_copy_24dp", "ic_content_copy_24dp", "ic_content_copy_white_a60_24dp")
            .map { Utils.getResId(it, "drawable") }
            .firstOrNull { it != 0 }
            ?: return null
        return ContextCompat.getDrawable(ctx, resId)?.mutate()?.also {
            it.setTint(ColorCompat.getThemedColor(ctx, R.b.colorInteractiveNormal))
        }
    }

    private fun sheetItem(
        ctx: Context,
        id: Int,
        label: String,
        icon: Drawable?,
        onClick: View.OnClickListener,
    ): TextView =
        TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Icon).apply {
            this.id = id
            text = label
            setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null)
            setOnClickListener(onClick)
        }

    companion object {
        private const val MAX_MESSAGES = 100
    }
}
