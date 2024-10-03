package com.anready.croissant.providers

import android.Manifest
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.database.MatrixCursor
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import com.anready.croissant.Constants.APPS_READ_ACCESS
import com.anready.croissant.Constants.LOGS
import com.anready.croissant.Constants.OPEN_FILES
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Files : ContentProvider() {
    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?,
    ): Cursor {
        val cursor = MatrixCursor(arrayOf("response"))
        val path = uri.getQueryParameter("path")
        val command = uri.getQueryParameter("command")

        val sharedPreferences = context?.getSharedPreferences(LOGS, Context.MODE_PRIVATE)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formattedDate = dateFormat.format(Date())

        sharedPreferences?.edit()?.putString("$formattedDate $callingPackage MSG_CODE: 00", command)?.apply()

        when (command) {
            "list" -> {
                cursor.addRow(arrayOf(listObjects(path.toString())))
            }
            "isPermissionsGranted" -> {
                cursor.addRow(arrayOf(message("result", checkPermission())))
            }
            "pathExist" -> {
                cursor.addRow(arrayOf(isPathExist(path.toString())))
            }
            "accessToCroissant" -> {
                cursor.addRow(arrayOf(accessToCroissant()))
            }
            "accessToOpenFiles" -> {
                cursor.addRow(arrayOf(accessToOpenFiles()))
            }
            else -> {
                cursor.addRow(arrayOf(message("error", "ERR_03: Unknown command")))
            }
        }

        return cursor
    }

    private fun accessToOpenFiles(): String {
        return message("result", context?.getSharedPreferences(OPEN_FILES, Context.MODE_PRIVATE)?.getBoolean(callingPackage, false) == true)
    }

    private fun accessToCroissant(): String {
        return message("result", context?.getSharedPreferences(APPS_READ_ACCESS, Context.MODE_PRIVATE)?.getBoolean(callingPackage, false) == true)
    }

    private fun message(type: String, message: Any): String {
        val error = JSONObject()
        error.put(type, message)
        return JSONArray().put(error).toString()
    }

    private fun checkPermission(): Boolean {
        val context = context ?: return false

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formattedDate = dateFormat.format(Date())

        val sharedPreferences = context.getSharedPreferences(LOGS, Context.MODE_PRIVATE)

        sharedPreferences?.edit()?.putString("$formattedDate $callingPackage", "Checking permission")?.apply()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun listObjects(path: String): String {
        val sharedPreferences = context?.getSharedPreferences(LOGS, Context.MODE_PRIVATE)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formattedDate = dateFormat.format(Date())

        if (context?.getSharedPreferences(APPS_READ_ACCESS, Context.MODE_PRIVATE)?.getBoolean(callingPackage, false) == false) {
            sharedPreferences?.edit()?.putString("$formattedDate $callingPackage ERR_CODE: 05", "No permission to access Croissant")?.apply()
            return message("error", "ERR_05: No permission to access Croissant")
        }


        if (!checkPermission()) {
            sharedPreferences?.edit()?.putString("$formattedDate $callingPackage ERR_CODE: 01", "Getting list of files in directory: $path")?.apply()
            return message("error", "ERR_01: No permission to access external storage")
        }

        sharedPreferences?.edit()?.putString("$formattedDate $callingPackage", "Getting list of files in directory: $path")?.apply()

        val files = File(Environment.getExternalStorageDirectory().absolutePath + path).listFiles()
        val filesArray = JSONArray()

        if (files == null) {
            sharedPreferences?.edit()?.putString("$formattedDate $callingPackage ERR_CODE: 02", "Getting list of files in directory: $path")?.apply()
            return message("error", "ERR_02: Incorrect path provided")
        }

        for (file in files) {
            val fileObject = JSONObject()
            fileObject.put("name", file.name)
            fileObject.put("type", file.isDirectory)
            fileObject.put("visibility", file.isHidden)
            fileObject.put("path", file.path)
            fileObject.put("absolutePath", file.absolutePath)
            fileObject.put("lastModified", file.lastModified())
            if (file.isDirectory) {
                fileObject.put("freeSpace", file.freeSpace)
            }
            filesArray.put(fileObject)
        }

        sharedPreferences?.edit()?.putString("$formattedDate $callingPackage ERR_CODE: 00", "Getting list of files in directory: $path")?.apply()
        return filesArray.toString()
    }

    private fun isPathExist(path: String): String {
        val sharedPreferences = context?.getSharedPreferences(LOGS, Context.MODE_PRIVATE)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formattedDate = dateFormat.format(Date())

        if (context?.getSharedPreferences(APPS_READ_ACCESS, Context.MODE_PRIVATE)?.getBoolean(callingPackage, true) == false) {
            sharedPreferences?.edit()?.putString("$formattedDate $callingPackage ERR_CODE: 05", "No permission to access Croissant")?.apply()
            return message("error", "ERR_05: No permission to access Croissant")
        }

        if (!checkPermission()) {
            sharedPreferences?.edit()?.putString("$formattedDate $callingPackage ERR_CODE: 01", "Is path exist: $path")?.apply()
            return message("error", "ERR_01: No permission to access external storage")
        }

        val file = File(Environment.getExternalStorageDirectory().absolutePath + path).exists()
        sharedPreferences?.edit()?.putString("$formattedDate $callingPackage ERR_CODE: 00", "Is path exist: $path")?.apply()
        return message("result", file)
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }
}
