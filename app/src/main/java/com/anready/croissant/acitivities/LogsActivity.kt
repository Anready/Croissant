package com.anready.croissant.acitivities

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.anready.croissant.Constants.LOGS
import com.anready.croissant.R
import com.anready.croissant.models.Log
import java.text.SimpleDateFormat
import java.util.Locale

class LogsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_logs)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        showLogs()

        val clearBtn = findViewById<android.widget.Button>(R.id.clear)
        val refreshBtn = findViewById<android.widget.Button>(R.id.refresh)

        clearBtn.setOnClickListener {
            clearLogs()
        }
        refreshBtn.setOnClickListener {
            showLogs()
        }
    }

    private fun showLogs() {
        val sharedPreferences = getSharedPreferences(LOGS, MODE_PRIVATE)
        val logsLv = findViewById<android.widget.ListView>(R.id.logsLv)

        val logsMap = sharedPreferences.all
        val logsList = ArrayList<Log>()
        val logsPreview = ArrayList<String>()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val sortedKeys = logsMap.keys.sortedByDescending { key ->
            val datePart = key.substring(0, 19)
            dateFormat.parse(datePart)
        }

        for (key in sortedKeys) {
            val value = logsMap[key]
            logsList.add(Log(key.substring(0, 19), key.substring(20, key.thirdIndexOf(" ")), value.toString()))
            logsPreview.add("$key: $value")
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, logsPreview)
        logsLv.adapter = adapter

        logsLv.setOnItemClickListener { _, _, position, _ ->
            val selectedLog = logsList[position]
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle(selectedLog.date)
            alertDialog.setMessage(
                getString(
                    R.string.app_action,
                    selectedLog.app,
                    selectedLog.action
                ))
            alertDialog.setPositiveButton("ОК") { dialog, _ ->
                dialog.dismiss()
            }
            alertDialog.show()
        }
    }


    private fun clearLogs() {
        val sharedPreferences = getSharedPreferences(LOGS, MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
        showLogs()
    }
}

private fun String.thirdIndexOf(s: String): Int {
    return if (this.indexOf(s, this.indexOf(s, this.indexOf(s) + 1) + 1) != -1) {
        this.indexOf(s, this.indexOf(s, this.indexOf(s) + 1) + 1)
    } else {
        this.length
    }
}
