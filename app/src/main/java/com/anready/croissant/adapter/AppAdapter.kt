package com.anready.croissant.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.google.android.material.switchmaterial.SwitchMaterial
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.anready.croissant.Constants.APPS_READ_ACCESS
import com.anready.croissant.Constants.OPEN_FILES
import com.anready.croissant.R
import com.anready.croissant.models.AppModel

class AppAdapter(private val context: Context, private val appList: List<AppModel>) : BaseAdapter() {

    override fun getCount(): Int = appList.size

    override fun getItem(position: Int): Any = appList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.select_file_item, parent, false)

        val appIcon = view.findViewById<ImageView>(R.id.imageView)
        val appName = view.findViewById<TextView>(R.id.textView)

        val app = appList[position]
        appIcon.setImageDrawable(app.icon)
        "${app.name} (${app.packageName})".also { appName.text = it }

        view.setOnClickListener {
            showAppDialog(context, app)
        }

        return view
    }

    private fun showAppDialog(context: Context, app: AppModel) {
        val builder = AlertDialog.Builder(context)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_layout, null)

        val read = context.getSharedPreferences(APPS_READ_ACCESS, Context.MODE_PRIVATE)
        val open = context.getSharedPreferences(OPEN_FILES, Context.MODE_PRIVATE)

        val icon = dialogView.findViewById<ImageView>(R.id.imageView2)
        icon.setImageDrawable(app.icon)

        val name = dialogView.findViewById<TextView>(R.id.textView2)
        "${app.name} (${app.packageName})".also { name.text = it }

        val switchButton = dialogView.findViewById<SwitchMaterial>(R.id.switchButton)
        switchButton.isChecked = read.getBoolean(app.packageName, false)
        switchButton.setOnCheckedChangeListener { _, isChecked ->
            read.edit().putBoolean(app.packageName, isChecked).apply()
        }

        val openFiles = dialogView.findViewById<SwitchMaterial>(R.id.switchButton2)
        openFiles.isChecked = open.getBoolean(app.packageName, false)
        openFiles.setOnCheckedChangeListener { _, isChecked ->
            open.edit().putBoolean(app.packageName, isChecked).apply()
        }

        builder.setView(dialogView)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = builder.create()
        dialog.show()
    }
}
