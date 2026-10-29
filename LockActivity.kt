package com.example.screenbreak

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class LockActivity : AppCompatActivity() {

    private lateinit var prefs: Prefs
    private lateinit var timeText: TextView
    private val handler = Handler(Looper.getMainLooper())

    private val tick = object : Runnable {
        override fun run() {
            val now = System.currentTimeMillis()
            if (!prefs.isLocked(now)) { finish(); return }
            val left = prefs.lockUntil - now
            timeText.text = when {
                left > 0 -> "%02d:%02d".format(left / 60000, (left / 1000) % 60)
                prefs.dailyExceeded(now) -> "Daily limit reached"
                else -> "Bedtime"
            }
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        prefs = Prefs(this)

        timeText = TextView(this).apply { textSize = 40f; gravity = Gravity.CENTER }
        val msg = TextView(this).apply {
            text = "Time for a break\nPhone calls and notifications still work."
            textSize = 20f; gravity = Gravity.CENTER
        }
        val wa = Button(this).apply {
            text = "Open WhatsApp"
            setOnClickListener {
                packageManager.getLaunchIntentForPackage("com.whatsapp")?.let { startActivity(it) }
            }
            if (!prefs.allowWhatsApp) visibility = View.GONE
        }
        val parent = Button(this).apply {
            text = "Parent unlock"
            setOnClickListener { askPin() }
        }
        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
            addView(msg); addView(timeText); addView(wa); addView(parent)
        })

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { }
        })
        handler.post(tick)
    }

    private fun askPin() {
        val input = EditText(this).apply {
            hint = "PIN"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        }
        AlertDialog.Builder(this).setTitle("Parent PIN").setView(input)
            .setPositiveButton("OK") { _, _ ->
                if (prefs.parentPin.isNotEmpty() && input.text.toString() == prefs.parentPin) {
                    prefs.lockUntil = 0L
                    prefs.snoozeUntil = System.currentTimeMillis() + 30 * 60_000L
                    finish()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroy() { handler.removeCallbacks(tick); super.onDestroy() }
}
