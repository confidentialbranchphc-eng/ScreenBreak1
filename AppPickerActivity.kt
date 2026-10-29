package com.example.screenbreak

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class AppPickerActivity : AppCompatActivity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val prefs = Prefs(this)
        val pm = packageManager

        val apps = pm.queryIntentActivities(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
            .map { it.activityInfo.packageName to it.loadLabel(pm).toString() }
            .filter { it.first != packageName }
            .distinctBy { it.first }
            .sortedBy { it.second.lowercase() }

        val chosen = prefs.extraAllowed.toMutableSet()
        val list = ListView(this)
        list.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        list.adapter = ArrayAdapter(this,
            android.R.layout.simple_list_item_multiple_choice, apps.map { it.second })
        apps.forEachIndexed { i, a -> list.setItemChecked(i, a.first in chosen) }
        list.setOnItemClickListener { _, _, i, _ ->
            if (list.isItemChecked(i)) chosen.add(apps[i].first) else chosen.remove(apps[i].first)
            prefs.extraAllowed = chosen
        }
        setContentView(list)
    }
}
