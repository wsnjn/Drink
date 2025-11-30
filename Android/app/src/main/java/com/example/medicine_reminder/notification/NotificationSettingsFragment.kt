package com.example.medicine_reminder.notification

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.medicine_reminder.R
import com.example.medicine_reminder.service.AlarmScheduler

class NotificationSettingsFragment : Fragment() {
    
    // 全局设置
    private lateinit var switchNotificationEnabled: Switch
    private lateinit var switchVibrationEnabled: Switch
    private lateinit var switchSoundEnabled: Switch
    private lateinit var tvNotificationEnabled: TextView
    private lateinit var tvVibrationEnabled: TextView
    private lateinit var tvSoundEnabled: TextView
    private lateinit var btnAddNotification: Button
    private lateinit var btnManageAlarms: Button
    private lateinit var btnTestAlarm: Button
    private lateinit var alarmScheduler: AlarmScheduler

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notification_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnAddNotification = view.findViewById(R.id.btnAddNotification)
        btnManageAlarms = view.findViewById(R.id.btnManageAlarms)
        btnTestAlarm = view.findViewById(R.id.btnTestAlarm)
        
        // 初始化闹钟调度器
        alarmScheduler = AlarmScheduler(requireContext())
        
        // 全局设置
        switchNotificationEnabled = view.findViewById(R.id.switchNotificationEnabled)
        switchVibrationEnabled = view.findViewById(R.id.switchVibrationEnabled)
        switchSoundEnabled = view.findViewById(R.id.switchSoundEnabled)
        tvNotificationEnabled = view.findViewById(R.id.tvNotificationEnabled)
        tvVibrationEnabled = view.findViewById(R.id.tvVibrationEnabled)
        tvSoundEnabled = view.findViewById(R.id.tvSoundEnabled)

        btnAddNotification.setOnClickListener {
            showNotificationContentDialog()
        }
        
        btnManageAlarms.setOnClickListener {
            showAlarmManagementDialog()
        }
        
        btnTestAlarm.setOnClickListener {
            testAlarm()
        }
        
        // 设置全局开关事件
        switchNotificationEnabled.setOnCheckedChangeListener { _, isChecked ->
            saveGlobalSettings("notification_enabled", isChecked)
        }
        switchVibrationEnabled.setOnCheckedChangeListener { _, isChecked ->
            saveGlobalSettings("vibration_enabled", isChecked)
        }
        switchSoundEnabled.setOnCheckedChangeListener { _, isChecked ->
            saveGlobalSettings("sound_enabled", isChecked)
        }

        loadGlobalSettings()
    }
    
    private fun loadGlobalSettings() {
        val sharedPrefs = requireContext().getSharedPreferences("notification_settings", Context.MODE_PRIVATE)
        
        val notificationEnabled = sharedPrefs.getBoolean("notification_enabled", true)
        val vibrationEnabled = sharedPrefs.getBoolean("vibration_enabled", true)
        val soundEnabled = sharedPrefs.getBoolean("sound_enabled", true)
        
        switchNotificationEnabled.isChecked = notificationEnabled
        switchVibrationEnabled.isChecked = vibrationEnabled
        switchSoundEnabled.isChecked = soundEnabled
    }
    
    private fun saveGlobalSettings(key: String, value: Boolean) {
        val sharedPrefs = requireContext().getSharedPreferences("notification_settings", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        editor.putBoolean(key, value)
        editor.apply()
        Toast.makeText(context, "设置已保存", Toast.LENGTH_SHORT).show()
    }

    private fun showNotificationContentDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_notification_content, null)
        val etNotificationMessage = dialogView.findViewById<android.widget.EditText>(R.id.etNotificationMessage)
        
        // 设置默认消息
        etNotificationMessage.setText("该服药了！")

        AlertDialog.Builder(context)
            .setTitle("设置通知内容")
            .setView(dialogView)
            .setPositiveButton("确定") { _, _ ->
                val message = etNotificationMessage.text.toString()
                if (message.isNotBlank()) {
                    saveNotificationMessage(message)
                    Toast.makeText(context, "通知内容已更新", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "请输入通知内容", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun saveNotificationMessage(message: String) {
        val sharedPrefs = requireContext().getSharedPreferences("notification_settings", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        editor.putString("notification_message", message)
        editor.apply()
    }
    
    /** 显示闹钟管理对话框 */
    private fun showAlarmManagementDialog() {
        val options = arrayOf(
            "🕐 设置所有闹钟",
            "❌ 取消所有闹钟",
            "🔄 重新设置闹钟",
            "📋 查看闹钟状态"
        )
        
        AlertDialog.Builder(context)
            .setTitle("闹钟管理")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        alarmScheduler.scheduleAllMedicineAlarms()
                        Toast.makeText(context, "已为所有药品设置闹钟（提前10分钟提醒）", Toast.LENGTH_LONG).show()
                    }
                    1 -> {
                        alarmScheduler.cancelAllAlarms()
                        Toast.makeText(context, "已取消所有闹钟", Toast.LENGTH_SHORT).show()
                    }
                    2 -> {
                        alarmScheduler.rescheduleAllAlarms()
                        Toast.makeText(context, "已重新设置所有闹钟", Toast.LENGTH_SHORT).show()
                    }
                    3 -> {
                        showAlarmStatus()
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    /** 显示闹钟状态 */
    private fun showAlarmStatus() {
        val medicines = getMedicinesFromStorage()
        val activeMedicines = medicines.filter { it.isActive }
        val totalAlarms = activeMedicines.sumOf { it.times.size }
        
        val statusMessage = buildString {
            appendLine("📊 闹钟状态")
            appendLine("━━━━━━━━━━━━━━━━━━━━")
            appendLine("📱 总药品数: ${medicines.size}")
            appendLine("✅ 启用药品: ${activeMedicines.size}")
            appendLine("🕐 总闹钟数: $totalAlarms")
            appendLine("⏰ 提醒时间: 提前10分钟")
            appendLine("")
            appendLine("📋 药品详情:")
            activeMedicines.forEach { medicine ->
                appendLine("• ${medicine.name}: ${medicine.times.joinToString(", ")}")
            }
        }
        
        AlertDialog.Builder(context)
            .setTitle("闹钟状态")
            .setMessage(statusMessage)
            .setPositiveButton("确定", null)
            .show()
    }
    
    /** 测试闹钟 */
    private fun testAlarm() {
        AlertDialog.Builder(context)
            .setTitle("测试闹钟")
            .setMessage("将立即触发测试闹钟，请确保手机音量开启")
            .setPositiveButton("开始测试") { _, _ ->
                // 立即触发测试闹钟
                val testIntent = android.content.Intent(requireContext(), com.example.medicine_reminder.service.MedicineAlarmReceiver::class.java).apply {
                    action = AlarmScheduler.ACTION_MEDICINE_REMINDER
                    putExtra(AlarmScheduler.EXTRA_MEDICINE_ID, "test")
                    putExtra(AlarmScheduler.EXTRA_MEDICINE_NAME, "测试药品")
                    putExtra(AlarmScheduler.EXTRA_MEDICINE_DOSAGE, "1片")
                    putExtra(AlarmScheduler.EXTRA_MEDICINE_TIME, "12:00")
                    putExtra(AlarmScheduler.EXTRA_REMINDER_TIME, "11:50")
                }
                
                // 直接发送广播
                requireContext().sendBroadcast(testIntent)
                
                Toast.makeText(context, "测试闹钟已触发", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    /** 从存储中获取药品数据 */
    private fun getMedicinesFromStorage(): List<com.example.medicine_reminder.model.Medicine> {
        val sharedPrefs = requireContext().getSharedPreferences("medicine_data", Context.MODE_PRIVATE)
        val json = sharedPrefs.getString("medicines", "[]")
        val type = object : com.google.gson.reflect.TypeToken<List<com.example.medicine_reminder.model.Medicine>>() {}.type
        return com.google.gson.Gson().fromJson(json, type)
    }
}
