package com.example.medicine_reminder.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.medicine_reminder.model.Medicine
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class AlarmScheduler(private val context: Context) {
    
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notificationSettings = NotificationManager(context)
    
    companion object {
        const val ACTION_MEDICINE_REMINDER = "com.example.medicine_reminder.MEDICINE_REMINDER"
        const val EXTRA_MEDICINE_ID = "medicine_id"
        const val EXTRA_MEDICINE_NAME = "medicine_name"
        const val EXTRA_MEDICINE_DOSAGE = "medicine_dosage"
        const val EXTRA_MEDICINE_TIME = "medicine_time"
        const val EXTRA_REMINDER_TIME = "reminder_time"
    }
    
    /** 为所有药品设置闹钟 */
    fun scheduleAllMedicineAlarms() {
        // 检查权限
        if (!hasAlarmPermission()) {
            Log.e("AlarmScheduler", "没有闹钟权限，无法设置闹钟")
            return
        }
        
        val medicines = getMedicinesFromStorage()
        medicines.filter { it.isActive }.forEach { medicine ->
            medicine.times.forEach { time ->
                scheduleMedicineAlarm(medicine, time)
            }
        }
        Log.d("AlarmScheduler", "已为所有药品设置闹钟")
    }
    
    /** 检查闹钟权限 */
    private fun hasAlarmPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }
    
    /** 为单个药品设置闹钟 */
    private fun scheduleMedicineAlarm(medicine: Medicine, medicineTime: String) {
        try {
            // 解析时间
            val timeParts = medicineTime.split(":")
            val hour = timeParts[0].toInt()
            val minute = timeParts[1].toInt()
            
            // 计算提醒时间（提前10分钟）
            val reminderTime = calculateReminderTime(hour, minute, 10)
            
            // 创建Intent
            val intent = Intent(context, MedicineAlarmReceiver::class.java).apply {
                action = ACTION_MEDICINE_REMINDER
                putExtra(EXTRA_MEDICINE_ID, medicine.id)
                putExtra(EXTRA_MEDICINE_NAME, medicine.name)
                putExtra(EXTRA_MEDICINE_DOSAGE, medicine.dosage)
                putExtra(EXTRA_MEDICINE_TIME, medicineTime)
                putExtra(EXTRA_REMINDER_TIME, reminderTime)
            }
            
            // 创建PendingIntent
            val requestCode = generateRequestCode(medicine.id, medicineTime)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // 设置闹钟
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, reminderTime.first)
                set(Calendar.MINUTE, reminderTime.second)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                
                // 如果时间已过，设置为明天
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
            
            Log.d("AlarmScheduler", "已设置闹钟: ${medicine.name} - $medicineTime (提醒时间: ${reminderTime.first}:${reminderTime.second.toString().padStart(2, '0')})")
            Log.d("AlarmScheduler", "闹钟触发时间: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(calendar.timeInMillis))}")
            Log.d("AlarmScheduler", "当前时间: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}")
            
        } catch (e: Exception) {
            Log.e("AlarmScheduler", "设置闹钟失败: ${medicine.name} - $medicineTime", e)
        }
    }
    
    /** 计算提醒时间（提前指定分钟） */
    private fun calculateReminderTime(hour: Int, minute: Int, beforeMinutes: Int): Pair<Int, Int> {
        var reminderHour = hour
        var reminderMinute = minute - beforeMinutes
        
        if (reminderMinute < 0) {
            reminderMinute += 60
            reminderHour -= 1
            if (reminderHour < 0) {
                reminderHour += 24
            }
        }
        
        return Pair(reminderHour, reminderMinute)
    }
    
    /** 生成请求码 */
    private fun generateRequestCode(medicineId: String, time: String): Int {
        return kotlin.math.abs(medicineId.hashCode() + time.hashCode())
    }
    
    /** 取消单个药品的闹钟 */
    fun cancelMedicineAlarm(medicineId: String, time: String) {
        val intent = Intent(context, MedicineAlarmReceiver::class.java)
        val requestCode = generateRequestCode(medicineId, time)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d("AlarmScheduler", "已取消闹钟: $medicineId - $time")
    }
    
    /** 取消所有闹钟 */
    fun cancelAllAlarms() {
        val medicines = getMedicinesFromStorage()
        medicines.forEach { medicine ->
            medicine.times.forEach { time ->
                cancelMedicineAlarm(medicine.id, time)
            }
        }
        Log.d("AlarmScheduler", "已取消所有闹钟")
    }
    
    /** 重新设置所有闹钟 */
    fun rescheduleAllAlarms() {
        cancelAllAlarms()
        scheduleAllMedicineAlarms()
    }
    
    /** 从存储中获取药品数据 */
    private fun getMedicinesFromStorage(): List<Medicine> {
        val sharedPrefs = context.getSharedPreferences("medicine_data", Context.MODE_PRIVATE)
        val json = sharedPrefs.getString("medicines", "[]")
        val type = object : TypeToken<List<Medicine>>() {}.type
        return Gson().fromJson(json, type)
    }
}
