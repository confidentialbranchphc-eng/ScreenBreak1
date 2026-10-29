package com.example.screenbreak

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/** Shown on the very first launch. The user must choose the break times and a parent PIN. */
class SetupActivity : AppCompatActivity() {

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val prefs = Prefs(this)

        var limit = 45   // minutes of continuous use
        var brk = 15     // minutes of break

        val title = TextView(this).apply {
            text = "Welcome to ScreenBreak"
            textSize = 26f
            gravity = Gravity.CENTER
        }
        val intro = TextView(this).apply {
            text = "Choose how long the phone can be used without a stop, and how long the break lasts. " +
                "During a break, only phone calls and WhatsApp (optional) work."
            setPadding(0, 24, 0, 24)
        }
        val limitLabel = TextView(this).apply { textSize = 18f }
        val breakLabel = TextView(this).apply { textSize = 18f }

        fun update() {
            limitLabel.text = "Use the phone for: $limit minutes"
            breakLabel.text = "Then lock for: $brk minutes"
        }
        update()

        fun listener(onChange: (Int) -> Unit) = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) { onChange(p); update() }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        }

        val limitBar = SeekBar(this).apply {
            max = 115; progress = limit - 5                     // 5 .. 120 minutes
            setOnSeekBarChangeListener(listener { limit = it + 5 })
        }
        val breakBar = SeekBar(this).apply {
            max = 59; progress = brk - 1                        // 1 .. 60 minutes
            setOnSeekBarChangeListener(listener { brk = it + 1 })
        }

        val kid = Button(this).apply {
            text = "Quick set: Kid (30 min use / 20 min break)"
            setOnClickListener { limitBar.progress = 30 - 5; breakBar.progress = 20 - 1 }
        }
        val adult = Button(this).apply {
            text = "Quick set: Adult (50 min use / 10 min break)"
            setOnClickListener { limitBar.progress = 50 - 5; breakBar.progress = 10 - 1 }
        }

        val pin = EditText(this).apply {
            hint = "Create parent PIN (4+ digits)"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        }
        val pin2 = EditText(this).apply {
            hint = "Repeat PIN"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        }

        val go = Button(this).apply {
            text = "Save and continue"
            setOnClickListener {
                val p1 = pin.text.toString()
                when {
                    p1.length < 4 ->
                        Toast.makeText(context, "PIN must be at least 4 digits", Toast.LENGTH_LONG).show()
                    p1 != pin2.text.toString() ->
                        Toast.makeText(context, "PINs do not match", Toast.LENGTH_LONG).show()
                    else -> {
                        prefs.limitMin = limit
                        prefs.breakMin = brk
                        prefs.parentPin = p1
                        prefs.setupDone = true
                        Toast.makeText(context,
                            "Saved. Now turn on blocking (button 1).", Toast.LENGTH_LONG).show()
                        startActivity(Intent(context, SettingsActivity::class.java))
                        finish()
                    }
                }
            }
        }

        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 64, 48, 48)
        }
        listOf(title, intro, limitLabel, limitBar, breakLabel, breakBar, kid, adult, pin, pin2, go)
            .forEach { col.addView(it) }
        setContentView(ScrollView(this).apply { addView(col) })
    }
}
