package com.example.screenbreak

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.telecom.TelecomManager
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.InputMethodManager

class BreakService : AccessibilityService() {

    private lateinit var prefs: Prefs
    private val handler = Handler(Looper.getMainLooper())

    private var usageStart = System.currentTimeMillis()
    private var lastScreenOff = 0L
    private var warned = false
    private var lastPkg: String? = null
    private val resetGapMs = 2 * 60_000L

    private val waPkgs = setOf("com.whatsapp", "com.whatsapp.w4b")
    private val allowed = mutableSetOf(
        "com.android.systemui",
        "com.android.server.telecom",
        "com.android.incallui",
        "com.google.android.dialer",
        "com.android.dialer",
        "com.samsung.android.incallui",
        "com.samsung.android.dialer"
    )

    private fun isAllowedPkg(pkg: String?): Boolean {
        if (pkg == null) return false
        if (pkg == packageName) return true
        if (pkg in waPkgs) return prefs.allowWhatsApp
        return pkg in allowed || pkg in prefs.extraAllowed
    }

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, i: Intent) {
            when (i.action) {
                Intent.ACTION_SCREEN_OFF -> lastScreenOff = System.currentTimeMillis()
                Intent.ACTION_SCREEN_ON -> {
                    val now = System.currentTimeMillis()
                    if (lastScreenOff != 0L && now - lastScreenOff > resetGapMs) {
                        usageStart = now
                        warned = false
                    }
                }
            }
        }
    }

    private val ticker = object : Runnable {
        override fun run() {
            val now = System.currentTimeMillis()
            val pm = getSystemService(PowerManager::class.java)
            if (pm.isInteractive && prefs.setupDone) {
                if (prefs.isBedtime(now) || prefs.dailyExceeded(now)) {
                    if (!isAllowedPkg(lastPkg)) showLock()
                } else {
                    if (now >= prefs.lockUntil) {
                        prefs.addUsage(10)
                        val remaining = prefs.limitMin * 60_000L - (now - usageStart)
                        if (remaining <= 0) startBreak(now)
                        else if (remaining <= 120_000L && !warned) { warned = true; warn() }
                    } else if (!isAllowedPkg(lastPkg)) {
                        showLock()
                    }
                }
            }
            handler.postDelayed(this, 10_000)
        }
    }

    override fun onServiceConnected() {
        prefs = Prefs(this)
        getSystemService(TelecomManager::class.java)?.defaultDialerPackage?.let { allowed += it }
        getSystemService(InputMethodManager::class.java)?.enabledInputMethodList
            ?.forEach { allowed += it.packageName }
        val f = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON); addAction(Intent.ACTION_SCREEN_OFF)
        }
        if (Build.VERSION.SDK_INT >= 33)
            registerReceiver(screenReceiver, f, Context.RECEIVER_NOT_EXPORTED)
        else registerReceiver(screenReceiver, f)
        handler.post(ticker)
    }

    private fun startBreak(now: Long) {
        prefs.lockUntil = now + prefs.breakMin * 60_000L
        usageStart = prefs.lockUntil
        warned = false
        if (!isAllowedPkg(lastPkg)) showLock()
    }

    private fun showLock() {
        startActivity(Intent(this, LockActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
    }

    private fun warn() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel("warn", "Break warning", NotificationManager.IMPORTANCE_HIGH))
        nm.notify(1, Notification.Builder(this, "warn")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Break in 2 minutes")
            .setContentText("Finish up - the phone will lock soon.")
            .setAutoCancel(true).build())
    }

    override fun onAccessibilityEvent(e: AccessibilityEvent) {
        if (e.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = e.packageName?.toString() ?: return
        lastPkg = pkg
        if (prefs.isLocked() && !isAllowedPkg(pkg)) showLock()
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        handler.removeCallbacks(ticker)
        try { unregisterReceiver(screenReceiver) } catch (_: Exception) {}
        super.onDestroy()
    }
}
