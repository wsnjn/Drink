package com.example.medicine_reminder.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.medicine_reminder.MainActivity
import com.example.medicine_reminder.R
import com.example.medicine_reminder.model.Medicine
import com.example.medicine_reminder.model.CheckinRecord
import com.example.medicine_reminder.model.ReminderStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class MedicineNotificationService(private val context: Context) {
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val notificationSettings = NotificationManager(context)
    private val channelId = "medicine_reminder_channel"
    private val channelName = "药品提醒"
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "药品服用提醒通知"
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun scheduleMedicineReminders() {
        // 检查通知是否启用
        if (!notificationSettings.isNotificationsEnabled()) {
            return
        }
        
        val medicines = getMedicinesFromStorage()
        val checkinRecords = getCheckinRecordsFromStorage()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        
        medicines.filter { it.isActive }.forEach { medicine ->
            medicine.times.forEach { time ->
                // 检查是否已经服用
                val checkin = checkinRecords.find {
                    it.medicineId == medicine.id &&
                    it.time == time &&
                    it.date == today
                }
                
                if (checkin == null) {
                    // 检查是否在提醒时间范围内
                    if (shouldSendReminder(time, currentTime)) {
                        sendMedicineReminder(medicine, time)
                    }
                }
            }
        }
    }
    
    private fun shouldSendReminder(medicineTime: String, currentTime: String): Boolean {
        val medicineHour = medicineTime.substring(0, 2).toInt()
        val medicineMinute = medicineTime.substring(3, 5).toInt()
        val currentHour = currentTime.substring(0, 2).toInt()
        val currentMinute = currentTime.substring(3, 5).toInt()
        
        val medicineTotalMinutes = medicineHour * 60 + medicineMinute
        val currentTotalMinutes = currentHour * 60 + currentMinute
        
        // 默认服药前1小时提醒
        val reminderBeforeMinutes = 60 // 1小时
        val reminderAfterMinutes = 30  // 服药后30分钟内还可以提醒
        
        val reminderStart = medicineTotalMinutes - reminderBeforeMinutes
        val reminderEnd = medicineTotalMinutes + reminderAfterMinutes
        
        return currentTotalMinutes >= reminderStart && currentTotalMinutes <= reminderEnd
    }
    
    private fun sendMedicineReminder(medicine: Medicine, time: String) {
        val notificationId = generateNotificationId(medicine.id, time)
        
        // 创建点击通知的Intent
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("action", "checkin")
            putExtra("medicine_id", medicine.id)
            putExtra("medicine_time", time)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 创建打卡Intent
        val checkinIntent = Intent(context, MedicineNotificationReceiver::class.java).apply {
            putExtra("medicine_id", medicine.id)
            putExtra("medicine_name", medicine.name)
            putExtra("medicine_time", time)
            putExtra("action", "checkin")
        }
        
        val checkinPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 1000,
            checkinIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 获取用户设置的通知消息
        val sharedPrefs = context.getSharedPreferences("notification_settings", Context.MODE_PRIVATE)
        val customMessage = sharedPrefs.getString("notification_message", "该服药了！")
        
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("💊 $customMessage")
            .setContentText("${medicine.name} (${medicine.dosage}) - $time")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$customMessage\n药品：${medicine.name}\n剂量：${medicine.dosage}\n时间：$time\n\n点击下方按钮快速打卡"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "✅ 已服用",
                checkinPendingIntent
            )
        
        // 根据用户设置添加声音和震动
        if (notificationSettings.isSoundEnabled()) {
            notificationBuilder.setDefaults(NotificationCompat.DEFAULT_SOUND)
        }
        
        if (notificationSettings.isVibrationEnabled()) {
            notificationBuilder.setVibrate(longArrayOf(0, 1000, 500, 1000))
        }
        
        notificationBuilder.setLights(0xFF4CAF50.toInt(), 1000, 1000)
        
        val notification = notificationBuilder.build()
        
        notificationManager.notify(notificationId, notification)
    }
    
    private fun generateNotificationId(medicineId: String, time: String): Int {
        return kotlin.math.abs(medicineId.hashCode() + time.hashCode())
    }
    
    fun cancelNotification(medicineId: String, time: String) {
        val notificationId = generateNotificationId(medicineId, time)
        notificationManager.cancel(notificationId)
    }
    
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
    
    private fun getMedicinesFromStorage(): List<Medicine> {
        val sharedPrefs = context.getSharedPreferences("medicine_data", Context.MODE_PRIVATE)
        val json = sharedPrefs.getString("medicines", "[]")
        val type = object : TypeToken<List<Medicine>>() {}.type
        return Gson().fromJson(json, type)
    }
    
    private fun getCheckinRecordsFromStorage(): List<CheckinRecord> {
        val sharedPrefs = context.getSharedPreferences("checkin_data", Context.MODE_PRIVATE)
        val json = sharedPrefs.getString("checkin_records", "[]")
        val type = object : TypeToken<List<CheckinRecord>>() {}.type
        return Gson().fromJson(json, type)
    }
}
