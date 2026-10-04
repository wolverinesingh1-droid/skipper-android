package com.skipper.adskip

import android.content.Context

object PauseList {

    private const val PREFS = "skipper_pause"
    private const val KEY_PACKAGES = "packages"

    // Built-in defaults — the service pauses when any of these is in front
    val DEFAULTS = listOf(
        "com.google.android.apps.walletnfcrel",
        "com.samsung.android.spay",
        "com.samsung.android.spayfw",
        "com.bitwarden.android",
        "com.onepassword.android",
        "com.lastpass.lpandroid",
        "com.google.android.apps.authenticator2",
        "com.chase.sig.android",
        "com.bankofamerica.cashpromobile",
        "com.wf.wellsfargomobile",
        "com.paypal.android.p2pmobile"
    )

    fun getAll(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val stored = prefs.getStringSet(KEY_PACKAGES, null)
        return if (stored == null) DEFAULTS else stored.toList().sorted()
    }

    fun add(context: Context, pkg: String): Boolean {
        val p = pkg.trim().lowercase()
        if (p.isBlank()) return false
        val current = getAll(context).toMutableSet()
        if (current.contains(p)) return false
        current.add(p)
        save(context, current)
        return true
    }

    fun remove(context: Context, pkg: String) {
        val current = getAll(context).toMutableSet()
        current.remove(pkg)
        save(context, current)
    }

    fun contains(context: Context, pkg: String): Boolean {
        return getAll(context).any { it.equals(pkg, ignoreCase = true) }
    }

    fun resetToDefaults(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_PACKAGES).apply()
    }

    private fun save(context: Context, packages: Set<String>) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_PACKAGES, packages).apply()
    }
}
