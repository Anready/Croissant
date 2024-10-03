package com.anready.croissant.acitivities

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.anready.croissant.BuildConfig
import com.anready.croissant.R
import com.anready.croissant.adapter.AppAdapter
import com.anready.croissant.models.AppModel

class AppControl : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_control)

        val listView = findViewById<ListView>(R.id.recyclerView)

        Thread {
            val appList = getInstalledApps()
            runOnUiThread {
                val adapter = AppAdapter(this, appList)
                listView.adapter = adapter
            }
        }.start()
    }

    private fun getInstalledApps(): List<AppModel> {
        val apps = mutableListOf<AppModel>()
        val pm = packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        for (appInfo in packages) {
            val appPackage = appInfo.packageName
            if ((appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0 && appPackage != BuildConfig.APPLICATION_ID) {
                val appName = pm.getApplicationLabel(appInfo).toString()
                val appIcon = pm.getApplicationIcon(appInfo)

                apps.add(AppModel(appName, appPackage, appIcon))
            }
        }

        return apps.sortedBy { it.name }
    }
}