package com.example.medicine_reminder.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.medicine_reminder.R
import com.example.medicine_reminder.MainActivity
import com.example.medicine_reminder.service.MedicineNotificationReceiver

class MedicineAlarmReceiver : BroadcastReceiver() {
    
    companion object {
        const val CHANNEL_ID = "medicine_alarm_channel"
        const val CHANNEL_NAME = "药品闹钟提醒"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("MedicineAlarmReceiver", "收到广播: ${intent.action}")
        
        if (intent.action == AlarmScheduler.ACTION_MEDICINE_REMINDER) {
            val medicineId = intent.getStringExtra(AlarmScheduler.EXTRA_MEDICINE_ID) ?: return
            val medicineName = intent.getStringExtra(AlarmScheduler.EXTRA_MEDICINE_NAME) ?: return
            val medicineDosage = intent.getStringExtra(AlarmScheduler.EXTRA_MEDICINE_DOSAGE) ?: return
            val medicineTime = intent.getStringExtra(AlarmScheduler.EXTRA_MEDICINE_TIME) ?: return
            val reminderTime = intent.getStringExtra(AlarmScheduler.EXTRA_REMINDER_TIME) ?: return
            
            Log.d("MedicineAlarmReceiver", "闹钟触发: $medicineName - $medicineTime")
            
            // 震动
            vibrate(context)
            
            // 显示通知
            showNotification(context, medicineId, medicineName, medicineDosage, medicineTime, reminderTime)
            
            // 显示弹窗（如果应用在前台）
            showAlertDialog(context, medicineName, medicineDosage, medicineTime)
        }
    }
    
    /** 震动 */
    private fun vibrate(context: Context) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrationPattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
                val vibrationEffect = VibrationEffect.createWaveform(vibrationPattern, -1)
                vibrator.vibrate(vibrationEffect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 1000, 500, 1000, 500, 1000), -1)
            }
            
            Log.d("MedicineAlarmReceiver", "震动已触发")
        } catch (e: Exception) {
            Log.e("MedicineAlarmReceiver", "震动失败", e)
        }
    }
    
    /** 显示通知 */
    private fun showNotification(
        context: Context,
        medicineId: String,
        medicineName: String,
        medicineDosage: String,
        medicineTime: String,
        reminderTime: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // 创建通知渠道
        createNotificationChannel(notificationManager)
        
        // 创建跳转到应用的Intent
        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val appPendingIntent = PendingIntent.getActivity(
            context,
            medicineId.hashCode(),
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 创建快速打卡Intent
        val checkinIntent = Intent(context, MedicineNotificationReceiver::class.java).apply {
            action = "com.example.medicine_reminder.CHECKIN"
            putExtra("medicine_id", medicineId)
            putExtra("medicine_time", medicineTime)
        }
        val checkinPendingIntent = PendingIntent.getBroadcast(
            context,
            medicineId.hashCode() + 1,
            checkinIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 构建通知
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("💊 药品提醒")
            .setContentText("$medicineName ($medicineDosage) - $medicineTime")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("该服药了！\n\n药品：$medicineName\n剂量：$medicineDosage\n时间：$medicineTime\n\n点击下方按钮快速打卡"))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(appPendingIntent)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "✅ 已服用",
                checkinPendingIntent
            )
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVibrate(longArrayOf(0, 1000, 500, 1000, 500, 1000))
            .setLights(0xFF4CAF50.toInt(), 1000, 1000)
            .build()
        
        notificationManager.notify(medicineId.hashCode(), notification)
        Log.d("MedicineAlarmReceiver", "通知已显示: $medicineName")
    }
    
    /** 创建通知渠道 */
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_MAX
            ).apply {
                description = "药品闹钟提醒通知"
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /** 显示弹窗（如果应用在前台） */
    private fun showAlertDialog(
        context: Context,
        medicineName: String,
        medicineDosage: String,
        medicineTime: String
    ) {
        try {
            // 这里可以添加弹窗逻辑
            // 由于BroadcastReceiver的限制，弹窗可能不会显示
            // 主要通过通知来提醒用户
            Log.d("MedicineAlarmReceiver", "尝试显示弹窗: $medicineName")
        } catch (e: Exception) {
            Log.e("MedicineAlarmReceiver", "显示弹窗失败", e)
        }
    }
}
