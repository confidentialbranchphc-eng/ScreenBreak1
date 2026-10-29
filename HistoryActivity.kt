package com.example.screenbreak

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HistoryActivity : AppCompatActivity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val prefs = Prefs(this)
        val rows = prefs.history(7).map { (day, min) -> "$day   -   ${min / 60}h ${min % 60}m" }
        val today = prefs.usedTodaySec() / 60
        val header = TextView(this).apply {
            text = "Today: ${today / 60}h ${today % 60}m"
            textSize = 24f
            setPadding(48, 48, 48, 24)
        }
        val list = ListView(this)
        list.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, rows)
        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(header); addView(list)
        })
    }
}
