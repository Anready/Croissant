package com.anready.croissant

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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.anready.croissant.adapter.FileUtils


class MainActivity : AppCompatActivity() {

    var path = "/"
    private lateinit var responseTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        responseTextView = findViewById(R.id.isPermissionGranted)
        responseTextView.text = if (checkPermission()) {
            "Permission granted"
        } else {
            "No permission"
        }

        if (!checkPermission()) {
            requestPermission()
            return
        }

        FileUtils.getObjectsByFolderId(this)
    }

    private fun requestPermission() {
        Toast.makeText(this, "Please, provide a permission", Toast.LENGTH_SHORT).show()
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
        if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            responseTextView.text = getString(R.string.no_permission)
            requestPermission()
        } else {
            responseTextView.text = getString(R.string.permission_granted)
            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
            FileUtils.getObjectsByFolderId(this)
        }
    }

    private val grantPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            responseTextView.text = getString(R.string.permission_granted)
            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
            FileUtils.getObjectsByFolderId(this)
        }
    }

    override fun onBackPressed() {
        if (path.lastIndexOf("/") > 0) {
            path = path.substring(0, path.lastIndexOf("/"))
            FileUtils.getObjectsByFolderId(this)
        } else {
            super.onBackPressed()
        }
    }
}