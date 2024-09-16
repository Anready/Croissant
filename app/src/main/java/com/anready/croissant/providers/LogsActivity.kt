package com.anready.croissant.providers

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.anready.croissant.Constants.LOGS
import com.anready.croissant.R

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
        val sharedPreferences = getSharedPreferences(LOGS, Context.MODE_PRIVATE)
        val logsTv = findViewById<android.widget.TextView>(R.id.logsTv)
        // leave 1 line after each log
        logsTv.text = sharedPreferences.all.toString().replace(", ", ",\n\n")
    }

    private fun clearLogs() {
        val sharedPreferences = getSharedPreferences(LOGS, Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
        showLogs()
    }
}
