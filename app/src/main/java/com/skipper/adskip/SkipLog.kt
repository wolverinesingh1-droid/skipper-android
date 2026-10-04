package com.skipper.adskip

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SkipLog {

    private const val TAG = "SkipLog"
    private const val FILE_NAME = "skip_log.json"
    private const val MAX_ENTRIES = 200

    data class Entry(
        val timestamp: Long,
        val description: String
    )

    private val entries = mutableListOf<Entry>()
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)
    private var loaded = false

    fun record(context: Context, description: String) {
        ensureLoaded(context)
        entries.add(0, Entry(System.currentTimeMillis(), description))
        while (entries.size > MAX_ENTRIES) entries.removeAt(entries.size - 1)
        save(context)
    }

    fun getAll(context: Context): List<Entry> {
        ensureLoaded(context)
        return entries.toList()
    }

    fun clear(context: Context) {
        entries.clear()
        save(context)
    }

    fun formatTime(entry: Entry): String {
        return timeFormat.format(Date(entry.timestamp))
    }

    private fun ensureLoaded(context: Context) {
        if (loaded) return
        loaded = true
        try {
            val file = File(context.filesDir, FILE_NAME)
            if (!file.exists()) return
            val text = file.readText()
            val arr = JSONArray(text)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                entries.add(
                    Entry(
                        obj.getLong("timestamp"),
                        obj.getString("description")
                    )
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load log", e)
        }
    }

    private fun save(context: Context) {
        try {
            val arr = JSONArray()
            for (entry in entries) {
                val obj = JSONObject()
                obj.put("timestamp", entry.timestamp)
                obj.put("description", entry.description)
                arr.put(obj)
            }
            File(context.filesDir, FILE_NAME).writeText(arr.toString())
        } catch (e: Exception) {
            Log.w(TAG, "Failed to save log", e)
        }
    }
}
