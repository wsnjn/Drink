package com.example.medicine_reminder.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.medicine_reminder.MainActivity
import com.example.medicine_reminder.model.CheckinRecord
import com.example.medicine_reminder.model.ReminderStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class MedicineNotificationReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "checkin" -> {
                val medicineId = intent.getStringExtra("medicine_id")
                val medicineName = intent.getStringExtra("medicine_name")
                val medicineTime = intent.getStringExtra("medicine_time")
                
                if (medicineId != null && medicineName != null && medicineTime != null) {
                    // 创建打卡记录
                    val checkinRecord = CheckinRecord(
                        medicineId = medicineId,
                        medicineName = medicineName,
                        date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                        time = medicineTime,
                        status = ReminderStatus.TAKEN,
                        timestamp = System.currentTimeMillis()
                    )
                    
                    // 保存打卡记录
                    saveCheckinRecord(context, checkinRecord)
                    
                    // 减少药品剩余数量
                    decreaseMedicineRemaining(context, medicineId)
                    
                    // 取消相关通知
                    val notificationService = MedicineNotificationService(context)
                    notificationService.cancelNotification(medicineId, medicineTime)
                    
                    // 显示确认消息
                    Toast.makeText(context, "✅ 已打卡: $medicineName - $medicineTime", Toast.LENGTH_LONG).show()
                    
                    // 发送广播通知其他组件更新
                    val updateIntent = Intent("com.example.medicine_reminder.CHECKIN_UPDATED")
                    context.sendBroadcast(updateIntent)
                }
            }
        }
    }
    
    private fun saveCheckinRecord(context: Context, checkinRecord: CheckinRecord) {
        val sharedPrefs = context.getSharedPreferences("checkin_data", Context.MODE_PRIVATE)
        val existingJson = sharedPrefs.getString("checkin_records", "[]")
        val type = object : TypeToken<List<CheckinRecord>>() {}.type
        val existingRecords = Gson().fromJson<List<CheckinRecord>>(existingJson, type).toMutableList()
        
        // 移除同一天同一时间的旧记录
        existingRecords.removeAll { 
            it.medicineId == checkinRecord.medicineId && 
            it.time == checkinRecord.time && 
            it.date == checkinRecord.date 
        }
        
        existingRecords.add(checkinRecord)
        
        val editor = sharedPrefs.edit()
        val json = Gson().toJson(existingRecords)
        editor.putString("checkin_records", json)
        editor.apply()
    }
    
    /** 减少药品剩余数量 */
    private fun decreaseMedicineRemaining(context: Context, medicineId: String) {
        val sharedPrefs = context.getSharedPreferences("medicine_data", Context.MODE_PRIVATE)
        val existingJson = sharedPrefs.getString("medicines", "[]")
        val type = object : TypeToken<List<com.example.medicine_reminder.model.Medicine>>() {}.type
        val medicines = Gson().fromJson<List<com.example.medicine_reminder.model.Medicine>>(existingJson, type).toMutableList()

        val index = medicines.indexOfFirst { it.id == medicineId }

        if (index != -1 && medicines[index].remaining > 0) {
            // 解析剂量中的数字部分
            val dosageAmount = extractDosageAmount(medicines[index].dosage)
            val newRemaining = maxOf(0.0, medicines[index].remaining - dosageAmount)
            
            medicines[index] = medicines[index].copy(
                remaining = newRemaining,
                updatedAt = System.currentTimeMillis()
            )

            val editor = sharedPrefs.edit()
            val json = Gson().toJson(medicines)
            editor.putString("medicines", json)
            editor.apply()
        }
    }
    
    /** 从剂量字符串中提取数字（支持小数） */
    private fun extractDosageAmount(dosage: String): Double {
        return try {
            // 提取字符串开头的数字（支持小数）
            val matchResult = Regex("""^(\d+(?:\.\d+)?)""").find(dosage)
            matchResult?.groupValues?.get(1)?.toDouble() ?: 1.0
        } catch (e: Exception) {
            1.0 // 如果解析失败，默认减1
        }
    }
}
