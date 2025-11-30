package com.example.medicine_reminder.service

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import java.util.*

class MedicineReminderScheduler : Service() {
    
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var notificationService: MedicineNotificationService
    private var isRunning = false
    
    private val checkRunnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                notificationService.scheduleMedicineReminders()
                // 每5分钟检查一次
                handler.postDelayed(this, 5 * 60 * 1000)
            }
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        notificationService = MedicineNotificationService(this)
        isRunning = true
        handler.post(checkRunnable)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isRunning) {
            isRunning = true
            handler.post(checkRunnable)
        }
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        handler.removeCallbacks(checkRunnable)
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}
