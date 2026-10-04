package com.skipper.adskip

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class PauseListActivity : AppCompatActivity() {

    private lateinit var listContainer: LinearLayout
    private lateinit var addInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pause_list)

        listContainer = findViewById(R.id.packageList)
        addInput = findViewById(R.id.addInput)

        findViewById<Button>(R.id.addButton).setOnClickListener {
            val pkg = addInput.text.toString().trim()
            if (pkg.isBlank()) return@setOnClickListener
            if (PauseList.add(this, pkg)) {
                addInput.text.clear()
                refresh()
            } else {
                Toast.makeText(this, "Already in list", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.resetButton).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Reset to defaults?")
                .setMessage("This will restore the built-in pause list and remove any custom packages.")
                .setPositiveButton("Reset") { _, _ ->
                    PauseList.resetToDefaults(this)
                    refresh()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        findViewById<Button>(R.id.backButton).setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        listContainer.removeAllViews()
        val packages = PauseList.getAll(this)
        if (packages.isEmpty()) {
            val tv = TextView(this)
            tv.text = "No packages. Add one below."
            tv.setTextColor(0xFF888888.toInt())
            tv.setPadding(0, 16, 0, 16)
            listContainer.addView(tv)
            return
        }
        for (pkg in packages) {
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            row.setPadding(0, 12, 0, 12)

            val label = TextView(this)
            label.text = pkg
            label.setTextColor(0xFFFFFFFF.toInt())
            label.textSize = 14f
            val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            label.layoutParams = lp
            row.addView(label)

            val remove = Button(this)
            remove.text = "Remove"
            remove.textSize = 12f
            remove.setOnClickListener {
                PauseList.remove(this, pkg)
                refresh()
            }
            row.addView(remove)

            listContainer.addView(row)
        }
    }
}
