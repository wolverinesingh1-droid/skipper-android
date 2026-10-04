package com.skipper.adskip

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings as AndroidSettings
import android.text.TextUtils
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var enableButton: Button
    private lateinit var viewLogButton: Button
    private lateinit var pauseListButton: Button
    private lateinit var waitGroup: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        enableButton = findViewById(R.id.enableButton)
        viewLogButton = findViewById(R.id.viewLogButton)
        pauseListButton = findViewById(R.id.pauseListButton)
        waitGroup = findViewById(R.id.waitGroup)

        val current = Settings.getWaitSeconds(this)
        val checkedId = when (current) {
            0 -> R.id.waitOff
            5 -> R.id.wait5
            10 -> R.id.wait15
            15 -> R.id.wait30
            else -> R.id.waitOff
        }
        waitGroup.check(checkedId)

        waitGroup.setOnCheckedChangeListener { _, id ->
            val seconds = when (id) {
                R.id.waitOff -> 0
                R.id.wait5 -> 5
                R.id.wait15 -> 10
                R.id.wait30 -> 15
                else -> 0
            }
            Settings.setWaitSeconds(this, seconds)
        }

        enableButton.setOnClickListener { openAccessibilitySettings() }
        viewLogButton.setOnClickListener {
            startActivity(Intent(this, LogActivity::class.java))
        }
        pauseListButton.setOnClickListener {
            startActivity(Intent(this, PauseListActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun updateStatus() {
        if (isServiceEnabled()) {
            statusText.text = getString(R.string.service_status_enabled)
            statusText.setTextColor(0xFF00AA00.toInt())
        } else {
            statusText.text = getString(R.string.service_status_disabled)
            statusText.setTextColor(0xFFAA0000.toInt())
        }
    }

    private fun isServiceEnabled(): Boolean {
        val expected = ComponentName(this, AdSkipperService::class.java)
        val enabledServices = AndroidSettings.Secure.getString(
            contentResolver,
            AndroidSettings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(enabledServices)
        while (splitter.hasNext()) {
            val component = ComponentName.unflattenFromString(splitter.next())
            if (component == expected) return true
        }
        return false
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(AndroidSettings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
