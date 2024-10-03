package com.anready.croissant.providers

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.anready.croissant.BuildConfig
import com.anready.croissant.Constants.OPEN_FILES
import java.io.File

class OpenFile : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        if (getSharedPreferences(OPEN_FILES, Context.MODE_PRIVATE)?.getBoolean(callingPackage, false) == false) {
            setResult(6, Intent().putExtra("ERR", "ERR_06: No permission to open files"))
            this.finish()
            return
        }

        if (!checkPermission()) {
            Toast.makeText(this, "No permission", Toast.LENGTH_SHORT).show()
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
        val file = File(Environment.getExternalStorageDirectory().absolutePath + path)
        openFile(file)

        setResult(0)
        finish()
    }

    private fun openFile(file: File) {
        if (!file.exists()) {
            Toast.makeText(this, "No file", Toast.LENGTH_SHORT).show()
            return
        }

        val fileUri = Uri.fromFile(file)
        val contentResolver = contentResolver
        val mimeType = contentResolver.getType(fileUri)

        val uri = FileProvider.getUriForFile(
            this,
            "${BuildConfig.APPLICATION_ID}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }

        startActivity(intent)
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
            Toast.makeText(this, "No permission", Toast.LENGTH_SHORT).show()
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