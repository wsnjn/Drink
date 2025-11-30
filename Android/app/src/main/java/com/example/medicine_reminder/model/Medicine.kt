package com.example.medicine_reminder.model

import java.util.*

data class Medicine(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val dosage: String, // 剂量，如 "1片", "5ml"
    val frequency: Int, // 每天服用次数
    val times: List<String>, // 具体时间，如 ["08:00", "12:00", "18:00"]
    val remaining: Double, // 剩余数量（支持0.5粒）
    val unit: String, // 单位，如 "片", "ml", "粒"
    val notes: String = "", // 备注
    val isActive: Boolean = true, // 是否启用
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class MedicineReminder(
    val id: String = UUID.randomUUID().toString(),
    val medicineId: String,
    val medicineName: String,
    val scheduledTime: String, // 计划时间
    val actualTime: Long? = null, // 实际服药时间
    val status: ReminderStatus = ReminderStatus.PENDING, // 状态
    val date: String, // 日期 YYYY-MM-DD
    val notes: String = ""
)

enum class ReminderStatus {
    PENDING,    // 待服药
    TAKEN,      // 已服药
    SKIPPED,     // 已跳过
    EXPIRED      // 已过期
}

data class CheckinRecord(
    val id: String = UUID.randomUUID().toString(),
    val medicineId: String,
    val medicineName: String,
    val date: String, // YYYY-MM-DD
    val time: String, // HH:mm
    val status: ReminderStatus,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
