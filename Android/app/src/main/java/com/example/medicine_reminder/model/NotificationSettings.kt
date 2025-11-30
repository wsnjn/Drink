package com.example.medicine_reminder.model

import java.io.Serializable

data class NotificationSettings(
    val id: String,
    val name: String, // 通知名称
    val time: String, // 提醒时间，格式为 "HH:mm"
    var isEnabled: Boolean = true,
    val daysOfWeek: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7), // 1=周日, 2=周一, ..., 7=周六
    val message: String = "该服药了！", // 通知消息
    val soundEnabled: Boolean = true, // 是否启用声音
    val vibrationEnabled: Boolean = true, // 是否启用震动
    val createdAt: Long = System.currentTimeMillis()
) : Serializable
