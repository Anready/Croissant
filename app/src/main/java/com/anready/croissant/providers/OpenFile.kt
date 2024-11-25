package com.anready.croissant.providers

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.anready.croissant.BuildConfig
import com.anready.croissant.Constants.OPEN_FILES
import com.anready.croissant.R
import java.io.File

class OpenFile : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val textView = TextView(this).apply {
            text = getString(R.string.touch_to_close)
            textSize = 20f
            setTextColor(resources.getColor(R.color.black)) // Set text color to white
            setBackgroundColor(resources.getColor(R.color.white)) // Set background color to black for contrast
            gravity = android.view.Gravity.CENTER // Center the text in the view
        }

        // Create a FrameLayout to hold the TextView and detect touch events
        val frameLayout = FrameLayout(this).apply {
            addView(textView, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ))
        }

        setContentView(frameLayout) // Set the FrameLayout as the root view

        // Set an OnTouchListener on the root view to finish the activity on touch
        frameLayout.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                finish()  // Close the activity when touched
                true  // Consume the event
            } else {
                false
            }
        }

        var callingPackage = this.callingActivity?.packageName

        if (callingPackage == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                callingPackage = this.referrer?.authority.toString()
            }

            if (callingPackage == null) {
                setResult(6, Intent().putExtra("ERR", "ERR_07: Incorrect running of Croissant"))
                this.finish()
                return
            }
        }

        if (getSharedPreferences(OPEN_FILES, MODE_PRIVATE)?.getBoolean(callingPackage, false) == false && callingPackage != BuildConfig.APPLICATION_ID) {
            setResult(6, Intent().putExtra("ERR", "ERR_06: No permission to open files"))
            this.finish()
            return
        }

        if (!checkPermission()) {
            Toast.makeText(this, getString(R.string.no_permission), Toast.LENGTH_SHORT).show()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                grantPermissionLauncher.launch(intent)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    101
                )
            }
            return
        }

        val path = this.intent.getStringExtra("path")

        val sanitizedPath = path!!.trimStart('/')
        val file = File(Environment.getExternalStorageDirectory(), sanitizedPath)

        openFile(file)

        setResult(0)
       // finish() DO NOT EVEN TOUCH THIS FUCKING LINE!!! I WAS SEARCHING WHERE IS BUG ALMOST 2 HOURS, BUG HERE
    }

    private fun openFile(file: File) {
        if (!file.exists()) {
            Toast.makeText(this, "No file", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = FileProvider.getUriForFile(
            this,
            "${BuildConfig.APPLICATION_ID}.provider",
            file
        )

        val mimeType = contentResolver.getType(uri)
            ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)
            ?: "application/octet-stream"


        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(this, getString(R.string.no_app_to_open_this_file), Toast.LENGTH_SHORT).show()
        }
    }


    private fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, getString(R.string.no_permission), Toast.LENGTH_SHORT).show()
        } else {
            val path = this.intent.getStringExtra("path")
            val file = File(Environment.getExternalStorageDirectory().absolutePath + path)
            openFile(file)
            finish()
        }
    }

    private val grantPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val path = this.intent.getStringExtra("path")
            val file = File(Environment.getExternalStorageDirectory().absolutePath + path)
            openFile(file)
            finish()
        }
    }
}