package com.anready.croissant.adapter

import android.app.Activity
import android.content.Intent
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import com.anready.croissant.acitivities.MainActivity
import com.anready.croissant.adapter.FileUtils.getObjectsByFolderId
import com.anready.croissant.models.DirectoryContents
import com.anready.croissant.providers.OpenFile
import java.io.File

class ListObjectsAdapter(
    private val ac: Activity,
    private val entries: List<DirectoryContents>
) : BaseAdapter() {

    private val inflater: LayoutInflater = LayoutInflater.from(ac)

    override fun getCount(): Int {
        return entries.size
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            view = inflater.inflate(com.anready.croissant.R.layout.select_file_item, parent, false)
        }

        val textView = view!!.findViewById<TextView>(com.anready.croissant.R.id.textView)
        val imageView = view.findViewById<ImageView>(com.anready.croissant.R.id.imageView)

        textView.text = entries[position].name
        imageView.setImageResource(if (entries[position].isDirectory) com.anready.croissant.R.drawable.ic_folder else com.anready.croissant.R.drawable.ic_file)

        view.setOnClickListener {
            val act = ac as MainActivity
            if (entries[position].name == "..") {
                act.path = act.path.substring(0, act.path.lastIndexOf("/"))
                getObjectsByFolderId(ac)
            } else if (entries[position].isDirectory) {
                act.path = act.path + "/" + entries[position].name
                getObjectsByFolderId(ac)
            } else {
                ac.startActivity(
                    Intent(ac, OpenFile::class.java).putExtra(
                        "path",
                        act.path + "/" + entries[position].name
                    )
                )
            }
        }

        return view
    }
}

object FileUtils {
    fun getObjectsByFolderId(activity: Activity) {
        val act = activity as MainActivity
        val files =
            File(Environment.getExternalStorageDirectory().absolutePath + act.path).listFiles()
                ?: return

        val fullList = mutableListOf<DirectoryContents>()

        for (file in files) {
            fullList.add(
                DirectoryContents(
                    name = file.name,
                    isDirectory = file.isDirectory,
                    isHidden = file.isHidden
                )
            )
        }

        fullList.sortBy { it.name }
        if (act.path != "/") fullList.add(0, DirectoryContents("..", true, isHidden = false))

        val listView = activity.findViewById<ListView>(com.anready.croissant.R.id.listObjects)
        val adapter = ListObjectsAdapter(activity, fullList)
        activity.runOnUiThread { listView.adapter = adapter }
    }
}