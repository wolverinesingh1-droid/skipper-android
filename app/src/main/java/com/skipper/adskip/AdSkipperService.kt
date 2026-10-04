package com.skipper.adskip

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class AdSkipperService : AccessibilityService() {

    companion object {
        private const val TAG = "AdSkipper"
        private val YOUTUBE_PACKAGES = setOf(
            "com.google.android.youtube",
            "com.google.android.apps.youtube.music"
        )

        private val BAD_WORDS = listOf(
            "chapter", "segment", "next", "previous", "forward", "back",
            "10 seconds", "5 seconds", "double tap", "rewind", "queue",
            "playlist", "share", "save", "download", "subscribe", "like",
            "dislike", "comment", "settings", "menu", "close", "search",
            "home", "library", "trending", "subscriptions", "cast",
            "notification", "account", "profile", "more", "options",
            "overflow", "collapse", "expand"
        )

        private const val HEURISTIC_DELAY_MS = 2000L
        private const val HEURISTIC_WINDOW_MS = 15000L  // only 15s after exact match
        private const val CLICK_COOLDOWN_MS = 3000L     // max 1 click per 3s
    }

    private var adFirstSeenAt: Long = 0
    private var lastExactMatchAt: Long = 0
    private var lastClickAt: Long = 0
    private var pausedBySensitiveApp: Boolean = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "AdSkipper service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val pkg = event.packageName?.toString() ?: return

        if (PauseList.contains(applicationContext, pkg)) {
            if (!pausedBySensitiveApp) {
                pausedBySensitiveApp = true
                Log.i(TAG, "Paused: $pkg")
                SkipLog.record(applicationContext, "Paused for: $pkg")
            }
            return
        }

        if (pkg in YOUTUBE_PACKAGES) {
            if (pausedBySensitiveApp) {
                pausedBySensitiveApp = false
                Log.i(TAG, "Resumed: $pkg")
                SkipLog.record(applicationContext, "Resumed in: $pkg")
            }

            val root = rootInActiveWindow ?: return
            try {
                handleAdSkip(root)
            } catch (e: Exception) {
                Log.w(TAG, "Error processing event", e)
            }
        }
    }

    override fun onInterrupt() {
        Log.i(TAG, "AdSkipper interrupted")
    }

    private fun handleAdSkip(root: AccessibilityNodeInfo) {
        val now = System.currentTimeMillis()

        // First pass: exact match
        val exact = findSkipButtonExact(root)
        if (exact != null) {
            lastExactMatchAt = now
            processClick(exact, "exact")
            return
        }

        // No exact match. Only try heuristic if we saw an exact match recently.
        if (lastExactMatchAt == 0L) return

        val sinceLastExact = now - lastExactMatchAt
        if (sinceLastExact > HEURISTIC_WINDOW_MS) {
            // Ad is long over. Reset.
            lastExactMatchAt = 0
            adFirstSeenAt = 0
            return
        }

        if (sinceLastExact < HEURISTIC_DELAY_MS) return

        val heuristic = findSkipButtonHeuristic(root)
        if (heuristic != null) {
            processClick(heuristic, "heuristic")
        }
    }

    private fun processClick(node: AccessibilityNodeInfo, source: String) {
        val now = System.currentTimeMillis()

        // Rate-limit clicks
        if (now - lastClickAt < CLICK_COOLDOWN_MS) {
            return
        }

        if (adFirstSeenAt == 0L) {
            adFirstSeenAt = now
            Log.i(TAG, "[$source] Skip button appeared — timer started")
        }

        val waitMs = Settings.getWaitSeconds(applicationContext) * 1000L
        val elapsed = now - adFirstSeenAt

        if (elapsed < waitMs) {
            Log.i(TAG, "[$source] Waiting: ${elapsed / 1000}s / ${waitMs / 1000}s")
            return
        }

        if (performClickOn(node, elapsed, source)) {
            lastClickAt = now
            adFirstSeenAt = 0
            lastExactMatchAt = 0
        }
    }

    // ---------- Exact matching ----------

    private fun findSkipButtonExact(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        findNodeByExactText(root, "Skip Ad")?.let { return it }
        findNodeByExactText(root, "Skip ads")?.let { return it }
        findNodeByExactText(root, "Skip Ads")?.let { return it }
        findNodeByExactText(root, "Skip")?.let { return it }

        val skipIds = listOf(
            "com.google.android.youtube:id/skip_ad_button",
            "com.google.android.youtube:id/ad_skip_button",
            "com.google.android.youtube:id/skip_button",
            "com.google.android.apps.youtube.music:id/skip_ad_button",
            "com.google.android.apps.youtube.music:id/ad_skip_button"
        )
        for (id in skipIds) {
            val nodes = root.findAccessibilityNodeInfosByViewId(id)
            if (nodes.isNotEmpty()) return nodes[0]
        }

        findNodeByExactDesc(root, "Skip Ad")?.let { return it }
        findNodeByExactDesc(root, "Skip")?.let { return it }

        return null
    }

    private fun isBadText(text: String): Boolean {
        val lower = text.lowercase()
        for (bad in BAD_WORDS) if (lower.contains(bad)) return true
        return false
    }

    private fun findNodeByExactText(
        root: AccessibilityNodeInfo,
        target: String
    ): AccessibilityNodeInfo? {
        val nodes = root.findAccessibilityNodeInfosByText(target)
        for (node in nodes) {
            val nodeText = node.text?.toString()?.trim() ?: continue
            if (!nodeText.equals(target, ignoreCase = true)) continue
            if (isBadText(nodeText)) continue
            if (nodeText.contains("Skip in", ignoreCase = true)) continue
            return node
        }
        return null
    }

    private fun findNodeByExactDesc(
        root: AccessibilityNodeInfo,
        target: String
    ): AccessibilityNodeInfo? {
        val stack = ArrayDeque<AccessibilityNodeInfo>()
        stack.addLast(root)
        while (stack.isNotEmpty()) {
            val node = stack.removeLast()
            val desc = node.contentDescription?.toString()?.trim() ?: ""
            if (desc.equals(target, ignoreCase = true) && !isBadText(desc)) return node
            for (i in 0 until node.childCount) {
                val child = node.getChild(i) ?: continue
                stack.addLast(child)
            }
        }
        return null
    }

    // ---------- Heuristic ----------

    private fun findSkipButtonHeuristic(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val screen = Rect()
        root.getBoundsInScreen(screen)

        val minX = screen.left + (screen.width() * 0.6).toInt()
        val minY = screen.top + (screen.height() * 0.6).toInt()

        var best: AccessibilityNodeInfo? = null
        var bestScore = 0

        val stack = ArrayDeque<AccessibilityNodeInfo>()
        stack.addLast(root)
        while (stack.isNotEmpty()) {
            val node = stack.removeLast()
            for (i in 0 until node.childCount) {
                val child = node.getChild(i) ?: continue
                stack.addLast(child)
            }

            if (!node.isClickable) continue

            val bounds = Rect()
            node.getBoundsInScreen(bounds)
            if (!screen.contains(bounds)) continue
            if (bounds.left < minX) continue
            if (bounds.top < minY) continue

            val text = node.text?.toString()?.trim() ?: ""
            val desc = node.contentDescription?.toString()?.trim() ?: ""
            val label = if (text.isNotEmpty()) text else desc
            if (label.isEmpty()) continue

            val wordCount = label.split(" ").size
            if (wordCount > 3) continue
            if (label.length > 20) continue
            if (isBadText(label)) continue

            val score = bounds.left + bounds.top
            if (score > bestScore) {
                bestScore = score
                best = node
            }
        }

        return best
    }

    // ---------- Click ----------

    private fun performClickOn(
        node: AccessibilityNodeInfo,
        elapsedMs: Long,
        source: String
    ): Boolean {
        val waitedSec = (elapsedMs / 1000).toInt()

        if (node.isClickable && node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
            val label = describeNode(node)
            Log.i(TAG, "Clicked ($source,direct) after ${waitedSec}s: $label")
            SkipLog.record(applicationContext, "Skipped ($source) after ${waitedSec}s: $label")
            return true
        }

        var parent = node.parent
        var depth = 0
        while (parent != null && depth < 4) {
            if (parent.isClickable &&
                parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            ) {
                val label = describeNode(parent)
                Log.i(TAG, "Clicked ($source,parent+$depth) after ${waitedSec}s: $label")
                SkipLog.record(applicationContext, "Skipped ($source,parent) after ${waitedSec}s: $label")
                return true
            }
            parent = parent.parent
            depth++
        }
        return false
    }

    private fun describeNode(node: AccessibilityNodeInfo): String {
        val text = node.text?.toString()?.trim()
        val desc = node.contentDescription?.toString()?.trim()
        val cls = node.className?.toString()?.substringAfterLast('.') ?: "?"
        val viewId = node.viewIdResourceName?.substringAfterLast('/') ?: ""
        val bounds = Rect()
        node.getBoundsInScreen(bounds)

        val parts = mutableListOf<String>()
        parts.add(cls)
        if (!viewId.isNullOrEmpty()) parts.add("id=$viewId")
        if (!text.isNullOrEmpty()) parts.add("text=\"$text\"")
        if (!desc.isNullOrEmpty()) parts.add("desc=\"$desc\"")
        parts.add("at(${bounds.left},${bounds.top},${bounds.width()}x${bounds.height()})")
        return parts.joinToString(" ")
    }
}
