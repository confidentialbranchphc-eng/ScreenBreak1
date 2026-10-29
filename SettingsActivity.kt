package com.example.screenbreak

import android.Manifest
import android.app.TimePickerDialog
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefs: Prefs
    private lateinit var limit: EditText
    private lateinit var brk: EditText
    private lateinit var daily: EditText
    private lateinit var bedInfo: TextView
    private lateinit var status: TextView

    private fun serviceOn(): Boolean {
        val s = Settings.Secure.getString(contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        return s.contains("$packageName/")
    }

    override fun onResume() {
        super.onResume()
        if (::status.isInitialized)
            status.text = if (serviceOn()) "Blocking: ON" else "Blocking: OFF - tap button 1 below"
    }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        prefs = Prefs(this)

        if (!prefs.setupDone) {
            startActivity(Intent(this, SetupActivity::class.java))
            finish()
            return
        }

        if (Build.VERSION.SDK_INT >= 33)
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)

        limit = EditText(this).apply { hint = "Use limit (min)"; inputType = InputType.TYPE_CLASS_NUMBER }
        brk = EditText(this).apply { hint = "Break length (min)"; inputType = InputType.TYPE_CLASS_NUMBER }
        daily = EditText(this).apply { hint = "Daily limit (min, 0 = off)"; inputType = InputType.TYPE_CLASS_NUMBER }
        val pin = EditText(this).apply {
            hint = "Parent PIN (numbers)"; setText(prefs.parentPin)
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        bedInfo = TextView(this)
        status = TextView(this).apply { textSize = 18f; setPadding(0, 0, 0, 24) }

        fun refresh() {
            limit.setText("${prefs.limitMin}")
            brk.setText("${prefs.breakMin}")
            daily.setText("${prefs.dailyLimitMin}")
            bedInfo.text = "Bedtime: %02d:%02d to %02d:%02d".format(
                prefs.bedStart / 60, prefs.bedStart % 60, prefs.bedEnd / 60, prefs.bedEnd % 60)
        }
        refresh()

        fun btn(t: String, a: () -> Unit): Button =
            Button(this).apply { text = t; setOnClickListener { a() } }

        fun pickTime(cur: Int, set: (Int) -> Unit) {
            TimePickerDialog(this, { _, h, m -> set(h * 60 + m); refresh() },
                cur / 60, cur % 60, true).show()
        }

        val bedSwitch = Switch(this).apply {
            text = "Enable bedtime lock"; isChecked = prefs.bedEnabled
            setOnCheckedChangeListener { _, on -> prefs.bedEnabled = on }
        }
        val waSwitch = Switch(this).apply {
            text = "Allow opening WhatsApp during breaks"; isChecked = prefs.allowWhatsApp
            setOnCheckedChangeListener { _, on -> prefs.allowWhatsApp = on }
        }

        val views: List<android.view.View> = listOf(
            status,
            btn("Preset: Kid (30/20, bed 21:00-07:00)") {
                prefs.applyKid(); refresh(); bedSwitch.isChecked = true },
            btn("Preset: Adult (50/10)") {
                prefs.applyAdult(); refresh(); bedSwitch.isChecked = false },
            limit, brk, daily, pin,
            bedSwitch, bedInfo,
            btn("Set bedtime start") { pickTime(prefs.bedStart) { prefs.bedStart = it } },
            btn("Set bedtime end") { pickTime(prefs.bedEnd) { prefs.bedEnd = it } },
            waSwitch,
            btn("Allowed apps...") { startActivity(Intent(this, AppPickerActivity::class.java)) },
            btn("Usage history") { startActivity(Intent(this, HistoryActivity::class.java)) },
            btn("Save") {
                prefs.limitMin = limit.text.toString().toIntOrNull()?.coerceIn(5, 180) ?: 45
                prefs.breakMin = brk.text.toString().toIntOrNull()?.coerceIn(1, 240) ?: 15
                prefs.dailyLimitMin = daily.text.toString().toIntOrNull()?.coerceIn(0, 1440) ?: 0
                prefs.parentPin = pin.text.toString()
                refresh()
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
            },
            btn("1) Enable blocking (Accessibility)") {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            },
            btn("2) Protect from uninstall (Device Admin)") {
                startActivity(Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                    .putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                        ComponentName(this, AdminReceiver::class.java))
                    .putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                        "Makes ScreenBreak harder to uninstall."))
            }
        )

        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }
        views.forEach { col.addView(it) }
        setContentView(ScrollView(this).apply { addView(col) })
    }
}
