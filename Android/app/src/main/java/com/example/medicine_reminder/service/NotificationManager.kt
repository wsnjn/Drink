package com.example.medicine_reminder.service

import android.content.Context
import android.content.SharedPreferences

class NotificationManager(private val context: Context) {
    
    private val sharedPrefs: SharedPreferences = 
        context.getSharedPreferences("notification_settings", Context.MODE_PRIVATE)
    
    companion object {
        const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        const val KEY_REMINDER_BEFORE_MINUTES = "reminder_before_minutes"
        const val KEY_REMINDER_AFTER_MINUTES = "reminder_after_minutes"
        const val KEY_SOUND_ENABLED = "sound_enabled"
        const val KEY_VIBRATION_ENABLED = "vibration_enabled"
    }
    
    fun isNotificationsEnabled(): Boolean {
        return sharedPrefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }
    
    fun setNotificationsEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }
    
    fun getReminderBeforeMinutes(): Int {
        return sharedPrefs.getInt(KEY_REMINDER_BEFORE_MINUTES, 60) // 默认提前1小时
    }
    
    fun setReminderBeforeMinutes(minutes: Int) {
        sharedPrefs.edit().putInt(KEY_REMINDER_BEFORE_MINUTES, minutes).apply()
    }
    
    fun getReminderAfterMinutes(): Int {
        return sharedPrefs.getInt(KEY_REMINDER_AFTER_MINUTES, 30) // 默认延后30分钟
    }
    
    fun setReminderAfterMinutes(minutes: Int) {
        sharedPrefs.edit().putInt(KEY_REMINDER_AFTER_MINUTES, minutes).apply()
    }
    
    fun isSoundEnabled(): Boolean {
        return sharedPrefs.getBoolean(KEY_SOUND_ENABLED, true)
    }
    
    fun setSoundEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
    }
    
    fun isVibrationEnabled(): Boolean {
        return sharedPrefs.getBoolean(KEY_VIBRATION_ENABLED, true)
    }
    
    fun setVibrationEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_VIBRATION_ENABLED, enabled).apply()
    }
}
