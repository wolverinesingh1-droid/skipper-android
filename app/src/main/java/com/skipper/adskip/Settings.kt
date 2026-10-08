package com.skipper.adskip

import android.content.Context

object Settings {

    private const val PREFS = "skipper_settings"
    private const val KEY_WAIT_SECONDS = "wait_seconds"

    const val DEFAULT_WAIT_SECONDS = 0

    fun getWaitSeconds(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_WAIT_SECONDS, DEFAULT_WAIT_SECONDS)
    }

    fun setWaitSeconds(context: Context, seconds: Int) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_WAIT_SECONDS, seconds).apply()
    }
}
