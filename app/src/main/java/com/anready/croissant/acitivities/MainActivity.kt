package com.anready.croissant.acitivities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.anready.croissant.R
import com.anready.croissant.adapter.FileUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity : AppCompatActivity() {

    var path = "/"
    private lateinit var responseTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        responseTextView = findViewById(R.id.isPermissionGranted)
        responseTextView.text = if (checkPermission()) {
            getString(R.string.permission_granted)
        } else {
            getString(R.string.no_permission)
        }

        if (!checkPermission()) {
            requestPermission()
        }

        if (savedInstanceState != null) {
            path = savedInstanceState.getString("N", "/")
        }

        FileUtils.getObjectsByFolderId(this)

        findViewById<FloatingActionButton>(R.id.floatingActionButton).setOnClickListener {
            startActivity(Intent(this, LogsActivity::class.java))
        }

        findViewById<FloatingActionButton>(R.id.floatingActionButton2).setOnClickListener {
            startActivity(Intent(this, AppControl::class.java))
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (path.lastIndexOf("/") > 0) {
                    path = path.substring(0, path.lastIndexOf("/"))
                    FileUtils.getObjectsByFolderId(this@MainActivity)
                } else {
                    finish()
                }
            }
        })
    }

    private fun requestPermission() {
        Toast.makeText(this, getString(R.string.please_provide_a_permission), Toast.LENGTH_SHORT).show()
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

        if (requestCode != 101) {
            return
        }

        if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            responseTextView.text = getString(R.string.no_permission)
            requestPermission()
        } else {
            responseTextView.text = getString(R.string.permission_granted)
            Toast.makeText(this, getString(R.string.permission_granted), Toast.LENGTH_SHORT).show()
            FileUtils.getObjectsByFolderId(this)
        }

        responseTextView.text = if (checkPermission()) {
            getString(R.string.permission_granted)
        } else {
            getString(R.string.no_permission)
        }
    }

    private val grantPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            responseTextView.text = getString(R.string.permission_granted)
            Toast.makeText(this, getString(R.string.permission_granted), Toast.LENGTH_SHORT).show()
            FileUtils.getObjectsByFolderId(this)

            responseTextView.text = if (checkPermission()) {
                getString(R.string.permission_granted)
            } else {
                getString(R.string.no_permission)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("N", path)
    }


    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        path = savedInstanceState.getString("N", "/")
    }
}
