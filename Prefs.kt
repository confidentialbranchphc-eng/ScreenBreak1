package com.example.screenbreak

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Prefs(ctx: Context) {
    private val p = ctx.getSharedPreferences("sb", Context.MODE_PRIVATE)

    var limitMin: Int
        get() = p.getInt("limit", 45)
        set(v) = p.edit().putInt("limit", v).apply()
    var breakMin: Int
        get() = p.getInt("break", 15)
        set(v) = p.edit().putInt("break", v).apply()
    var parentPin: String
        get() = p.getString("pin", "") ?: ""
        set(v) = p.edit().putString("pin", v).apply()
    var lockUntil: Long
        get() = p.getLong("lockUntil", 0L)
        set(v) = p.edit().putLong("lockUntil", v).apply()

    var bedEnabled: Boolean
        get() = p.getBoolean("bedOn", false)
        set(v) = p.edit().putBoolean("bedOn", v).apply()
    var bedStart: Int
        get() = p.getInt("bedStart", 22 * 60)
        set(v) = p.edit().putInt("bedStart", v).apply()
    var bedEnd: Int
        get() = p.getInt("bedEnd", 6 * 60)
        set(v) = p.edit().putInt("bedEnd", v).apply()
    var snoozeUntil: Long
        get() = p.getLong("snooze", 0L)
        set(v) = p.edit().putLong("snooze", v).apply()

    var extraAllowed: Set<String>
        get() = p.getStringSet("extra", emptySet()) ?: emptySet()
        set(v) = p.edit().putStringSet("extra", HashSet(v)).apply()

    var setupDone: Boolean
        get() = p.getBoolean("setupDone", false)
        set(v) = p.edit().putBoolean("setupDone", v).apply()

    var dailyLimitMin: Int
        get() = p.getInt("daily", 0)
        set(v) = p.edit().putInt("daily", v).apply()
    var allowWhatsApp: Boolean
        get() = p.getBoolean("wa", true)
        set(v) = p.edit().putBoolean("wa", v).apply()

    private fun key(offsetDays: Int = 0): String {
        val t = System.currentTimeMillis() - offsetDays * 86_400_000L
        return "u_" + SimpleDateFormat("yyyyMMdd", Locale.US).format(Date(t))
    }

    fun addUsage(sec: Int) { val k = key(); p.edit().putInt(k, p.getInt(k, 0) + sec).apply() }
    fun usedTodaySec() = p.getInt(key(), 0)

    fun dailyExceeded(now: Long = System.currentTimeMillis()) =
        dailyLimitMin > 0 && now >= snoozeUntil && usedTodaySec() >= dailyLimitMin * 60

    fun history(days: Int = 7): List<Pair<String, Int>> = (0 until days).map { d ->
        val label = SimpleDateFormat("EEE dd MMM", Locale.getDefault())
            .format(Date(System.currentTimeMillis() - d * 86_400_000L))
        label to p.getInt(key(d), 0) / 60
    }

    fun isBedtime(now: Long = System.currentTimeMillis()): Boolean {
        if (!bedEnabled || now < snoozeUntil) return false
        val c = Calendar.getInstance()
        val m = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE)
        return if (bedStart <= bedEnd) m in bedStart until bedEnd
        else m >= bedStart || m < bedEnd
    }

    fun isLocked(now: Long = System.currentTimeMillis()) =
        setupDone && (now < lockUntil || isBedtime(now) || dailyExceeded(now))

    fun applyKid() { limitMin = 30; breakMin = 20; bedEnabled = true; bedStart = 21 * 60; bedEnd = 7 * 60 }
    fun applyAdult() { limitMin = 50; breakMin = 10; bedEnabled = false }
}
