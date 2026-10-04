package com.skipper.adskip

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LogActivity : AppCompatActivity() {

    private lateinit var logText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log)

        logText = findViewById(R.id.logText)

        findViewById<Button>(R.id.clearButton).setOnClickListener {
            SkipLog.clear(this)
            refresh()
        }

        findViewById<Button>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val entries = SkipLog.getAll(this)
        if (entries.isEmpty()) {
            logText.text = getString(R.string.log_empty)
            return
        }
        val sb = StringBuilder()
        for (entry in entries) {
            sb.append(SkipLog.formatTime(entry))
                .append("  ")
                .append(entry.description)
                .append("\n")
        }
        logText.text = sb.toString()
    }
}
