package com.example.medicine_reminder.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medicine_reminder.R
import com.example.medicine_reminder.model.Medicine
import com.example.medicine_reminder.model.CheckinRecord
import com.example.medicine_reminder.model.ReminderStatus
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {
    
    private lateinit var todayMedicinesCard: MaterialCardView
    private lateinit var tvTodayMedicines: TextView
    private lateinit var tvTakenCount: TextView
    private lateinit var tvPendingCount: TextView
    private lateinit var tvSkippedCount: TextView
    
    private lateinit var nextReminderCard: MaterialCardView
    private lateinit var tvNextReminder: TextView
    private lateinit var tvNextTime: TextView
    
    private lateinit var expandedMedicinesCard: MaterialCardView
    private lateinit var recyclerViewTodayMedicines: RecyclerView
    private lateinit var todayMedicinesAdapter: TodayMedicinesAdapter
    private var isExpanded = false
    
    private lateinit var expandedReminderCard: MaterialCardView
    private lateinit var recyclerViewReminderMedicines: RecyclerView
    private lateinit var reminderMedicinesAdapter: TodayMedicinesAdapter
    private var isReminderExpanded = false
    
    private val checkinUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.medicine_reminder.CHECKIN_UPDATED") {
                // 刷新数据
                loadTodayMedicinesData()
                if (isExpanded) {
                    loadTodayMedicinesExpanded()
                }
            }
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 绑定UI
        todayMedicinesCard = view.findViewById(R.id.todayMedicinesCard)
        tvTodayMedicines = view.findViewById(R.id.tvTodayMedicines)
        tvTakenCount = view.findViewById(R.id.tvTakenCount)
        tvPendingCount = view.findViewById(R.id.tvPendingCount)
        tvSkippedCount = view.findViewById(R.id.tvSkippedCount)
        
        nextReminderCard = view.findViewById(R.id.nextReminderCard)
        tvNextReminder = view.findViewById(R.id.tvNextReminder)
        tvNextTime = view.findViewById(R.id.tvNextTime)
        
        expandedMedicinesCard = view.findViewById(R.id.expandedMedicinesCard)
        recyclerViewTodayMedicines = view.findViewById(R.id.recyclerViewTodayMedicines)
        
        expandedReminderCard = view.findViewById(R.id.expandedReminderCard)
        recyclerViewReminderMedicines = view.findViewById(R.id.recyclerViewReminderMedicines)
        
        // 设置点击事件
        todayMedicinesCard.setOnClickListener { toggleExpandedView() }
        nextReminderCard.setOnClickListener { toggleReminderExpandedView() }
        
        // 设置展开视图
        setupExpandedView()
        setupReminderExpandedView()
        
        // 加载数据
        loadTodayMedicinesData()
        loadNextReminderData()
    }

    override fun onResume() {
        super.onResume()
        // 注册广播接收器
        val filter = IntentFilter("com.example.medicine_reminder.CHECKIN_UPDATED")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(checkinUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            requireContext().registerReceiver(checkinUpdateReceiver, filter)
        }
        
        // 刷新数据
        loadTodayMedicinesData()
        loadNextReminderData()
    }
    
    override fun onPause() {
        super.onPause()
        // 注销广播接收器
        try {
            requireContext().unregisterReceiver(checkinUpdateReceiver)
        } catch (e: Exception) {
            // 忽略注销失败的错误
        }
    }
    
    /** 加载今日药品数据 */
    private fun loadTodayMedicinesData() {
        try {
            val medicines = getMedicinesFromStorage()
            val checkinRecords = getCheckinRecordsFromStorage()
            
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            
            // 收集所有时间段
            val allTimeSlots = mutableSetOf<String>()
            medicines.filter { it.isActive }.forEach { medicine ->
                medicine.times.forEach { time ->
                    allTimeSlots.add(time)
                }
            }
            
            var totalCount = allTimeSlots.size // 总时间段数
            var takenCount = 0
            var pendingCount = 0
            
            // 检查每个时间段是否已打卡
            allTimeSlots.forEach { timeSlot ->
                var hasTakenAny = false
                
                // 检查这个时间段是否有任何药品已打卡
                medicines.filter { it.isActive }.forEach { medicine ->
                    if (medicine.times.contains(timeSlot)) {
                        val checkin = checkinRecords.find {
                            it.medicineId == medicine.id &&
                            it.time == timeSlot &&
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.timestamp)) == today &&
                            it.status == ReminderStatus.TAKEN
                        }
                        if (checkin != null) {
                            hasTakenAny = true
                        }
                    }
                }
                
                if (hasTakenAny) {
                    takenCount++
                } else {
                    pendingCount++
                }
            }
            
            tvTodayMedicines.text = "今日应服药: $totalCount 次"
            tvTakenCount.text = "已服药: $takenCount 次"
            tvPendingCount.text = "待服药: $pendingCount 次"
            tvSkippedCount.text = "药品: ${medicines.filter { it.isActive }.size} 种"
            
        } catch (e: Exception) {
            tvTodayMedicines.text = "今日应服药: 加载失败"
            tvTakenCount.text = "已服药: --"
            tvPendingCount.text = "待服药: --"
            tvSkippedCount.text = "药品: --"
        }
    }
    
    /** 加载今日提醒数据 - 显示应该提醒次数 */
    private fun loadNextReminderData() {
        try {
            val medicines = getMedicinesFromStorage()
            val checkinRecords = getCheckinRecordsFromStorage()
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            // 收集所有时间段
            val allTimeSlots = mutableSetOf<String>()
            medicines.filter { it.isActive }.forEach { medicine ->
                medicine.times.forEach { time ->
                    allTimeSlots.add(time)
                }
            }
            
            var totalPendingCount = 0
            val pendingTimeSlots = mutableListOf<String>()

            // 检查每个时间段是否已打卡
            allTimeSlots.forEach { timeSlot ->
                var hasTakenAny = false
                
                // 检查这个时间段是否有任何药品已打卡
                medicines.filter { it.isActive }.forEach { medicine ->
                    if (medicine.times.contains(timeSlot)) {
                        val checkin = checkinRecords.find {
                            it.medicineId == medicine.id &&
                            it.time == timeSlot &&
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.timestamp)) == today &&
                            it.status == ReminderStatus.TAKEN
                        }
                        if (checkin != null) {
                            hasTakenAny = true
                        }
                    }
                }
                
                if (!hasTakenAny) {
                    totalPendingCount++
                    pendingTimeSlots.add(timeSlot)
                }
            }

            if (totalPendingCount > 0) {
                // 计算待服药时间段涉及的药品种类
                val pendingMedicineTypes = mutableSetOf<String>()
                pendingTimeSlots.forEach { timeSlot ->
                    medicines.filter { it.isActive }.forEach { medicine ->
                        if (medicine.times.contains(timeSlot)) {
                            pendingMedicineTypes.add(medicine.name)
                        }
                    }
                }
                
                tvNextReminder.text = "今日剩余提醒: $totalPendingCount 次"
                tvNextTime.text = "涉及 ${pendingMedicineTypes.size} 种药品"
            } else {
                tvNextReminder.text = "今日无更多提醒"
                tvNextTime.text = "所有药品已提醒完毕"
            }

        } catch (e: Exception) {
            tvNextReminder.text = "今日提醒: 加载失败"
            tvNextTime.text = "药品数量: --"
        }
    }
    
    /** 计算某个时间点还有几次没喝 */
    private fun calculateRemainingTimes(medicine: Medicine, time: String, checkinRecords: List<CheckinRecord>, today: String): Int {
        // 这里可以根据药品的服用频率来计算
        // 简化处理：如果今天这个时间点没喝，就算1次
        val checkin = checkinRecords.find {
            it.medicineId == medicine.id &&
            it.time == time &&
            it.date == today
        }
        return if (checkin == null) 1 else 0
    }
    
    private fun setupExpandedView() {
        todayMedicinesAdapter = TodayMedicinesAdapter { time, medicines ->
            showTimeSlotCheckinDialog(time, medicines)
        }
        recyclerViewTodayMedicines.layoutManager = LinearLayoutManager(context)
        recyclerViewTodayMedicines.adapter = todayMedicinesAdapter
        
        // 初始状态隐藏展开视图
        expandedMedicinesCard.visibility = View.GONE
    }
    
    private fun setupReminderExpandedView() {
        reminderMedicinesAdapter = TodayMedicinesAdapter { time, medicines ->
            showTimeSlotCheckinDialog(time, medicines)
        }
        recyclerViewReminderMedicines.layoutManager = LinearLayoutManager(context)
        recyclerViewReminderMedicines.adapter = reminderMedicinesAdapter
        
        // 初始状态隐藏展开视图
        expandedReminderCard.visibility = View.GONE
    }
    
    private fun toggleExpandedView() {
        isExpanded = !isExpanded
        if (isExpanded) {
            // 关闭提醒展开视图
            isReminderExpanded = false
            expandedReminderCard.visibility = View.GONE
            
            expandedMedicinesCard.visibility = View.VISIBLE
            loadTodayMedicinesExpanded()
        } else {
            expandedMedicinesCard.visibility = View.GONE
        }
    }
    
    private fun toggleReminderExpandedView() {
        isReminderExpanded = !isReminderExpanded
        if (isReminderExpanded) {
            // 关闭今日应服药展开视图
            isExpanded = false
            expandedMedicinesCard.visibility = View.GONE
            
            expandedReminderCard.visibility = View.VISIBLE
            loadReminderMedicinesExpanded()
        } else {
            expandedReminderCard.visibility = View.GONE
        }
    }
    
    private fun loadTodayMedicinesExpanded() {
        val medicines = getMedicinesFromStorage()
        val checkinRecords = getCheckinRecordsFromStorage()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        val timeGroups = mutableMapOf<String, MutableList<MedicineTimeItem>>()
        
        medicines.filter { it.isActive }.forEach { medicine ->
            medicine.times.forEach { time ->
                val checkin = checkinRecords.find {
                    it.medicineId == medicine.id &&
                    it.time == time &&
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.timestamp)) == today
                }
                
                val item = MedicineTimeItem(
                    medicine = medicine,
                    time = time,
                    isTaken = checkin?.status == ReminderStatus.TAKEN
                )
                
                // 只添加已服药的药品
                if (item.isTaken) {
                    if (!timeGroups.containsKey(time)) {
                        timeGroups[time] = mutableListOf()
                    }
                    timeGroups[time]?.add(item)
                }
            }
        }
        
        // 按时间排序
        val sortedGroups = timeGroups.toList().sortedBy { it.first }
        val expandedItems = mutableListOf<ExpandedMedicineItem>()
        
        sortedGroups.forEach { (time, items) ->
            expandedItems.add(ExpandedMedicineItem(time, items))
        }
        
        todayMedicinesAdapter.updateData(expandedItems)
    }
    
    private fun loadReminderMedicinesExpanded() {
        val medicines = getMedicinesFromStorage()
        val checkinRecords = getCheckinRecordsFromStorage()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        val timeGroups = mutableMapOf<String, MutableList<MedicineTimeItem>>()
        
        medicines.filter { it.isActive }.forEach { medicine ->
            medicine.times.forEach { time ->
                val checkin = checkinRecords.find {
                    it.medicineId == medicine.id &&
                    it.time == time &&
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.timestamp)) == today
                }
                
                val item = MedicineTimeItem(
                    medicine = medicine,
                    time = time,
                    isTaken = checkin?.status == ReminderStatus.TAKEN
                )
                
                // 只添加未服药的药品
                if (!item.isTaken) {
                    if (!timeGroups.containsKey(time)) {
                        timeGroups[time] = mutableListOf()
                    }
                    timeGroups[time]?.add(item)
                }
            }
        }
        
        // 按时间排序
        val sortedGroups = timeGroups.toList().sortedBy { it.first }
        val expandedItems = mutableListOf<ExpandedMedicineItem>()
        
        sortedGroups.forEach { (time, items) ->
            expandedItems.add(ExpandedMedicineItem(time, items))
        }
        
        reminderMedicinesAdapter.updateData(expandedItems)
    }
    
    private fun showTimeSlotCheckinDialog(time: String, medicines: List<MedicineTimeItem>) {
        // 过滤出未服用的药品
        val pendingMedicines = medicines.filter { !it.isTaken }
        
        if (pendingMedicines.isEmpty()) {
            Toast.makeText(context, "该时间段的所有药品已服用", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 为每个未服用的药品创建打卡记录
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val medicinesToUpdate = mutableListOf<Medicine>()
        
        pendingMedicines.forEach { medicineItem ->
            val checkinRecord = CheckinRecord(
                medicineId = medicineItem.medicine.id,
                medicineName = medicineItem.medicine.name,
                date = today,
                time = time,
                status = ReminderStatus.TAKEN,
                timestamp = System.currentTimeMillis()
            )
            saveCheckinRecord(checkinRecord)
            
            // 减少药品剩余数量
            decreaseMedicineRemaining(medicineItem.medicine)
            medicinesToUpdate.add(medicineItem.medicine)
        }
        
        val medicineNames = pendingMedicines.joinToString("、") { it.medicine.name }
        Toast.makeText(context, "✅ 已打卡: $time - $medicineNames", Toast.LENGTH_SHORT).show()
        
        // 刷新界面
        loadTodayMedicinesExpanded() // 刷新已服药列表
        loadReminderMedicinesExpanded() // 刷新待服药列表
        loadTodayMedicinesData() // 刷新统计数据
        loadNextReminderData() // 刷新下次提醒
    }
    
    private fun showCheckinDialog(medicine: Medicine, time: String) {
        val checkinRecord = CheckinRecord(
            medicineId = medicine.id,
            medicineName = medicine.name,
            date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            time = time,
            status = ReminderStatus.TAKEN,
            timestamp = System.currentTimeMillis()
        )
        saveCheckinRecord(checkinRecord)
        
        // 减少药品剩余数量
        decreaseMedicineRemaining(medicine)
        
        Toast.makeText(context, "✅ 已打卡: ${medicine.name} - $time", Toast.LENGTH_SHORT).show()
        loadTodayMedicinesExpanded() // 刷新展开视图
        loadTodayMedicinesData() // 刷新统计数据
        loadNextReminderData() // 刷新下次提醒
    }
    
    /** 减少药品剩余数量 */
    private fun decreaseMedicineRemaining(medicine: Medicine) {
        val medicines = getMedicinesFromStorage().toMutableList()
        val index = medicines.indexOfFirst { it.id == medicine.id }
        
        if (index != -1 && medicines[index].remaining > 0) {
            // 解析剂量中的数字部分（支持小数）
            val dosageAmount = extractDosageAmount(medicine.dosage)
            val oldRemaining = medicines[index].remaining
            val newRemaining = maxOf(0.0, medicines[index].remaining - dosageAmount)
            
            // 添加调试信息
            android.util.Log.d("MedicineUpdate", "药品: ${medicine.name}, 剂量: ${medicine.dosage}, 提取: $dosageAmount, 剩余: $oldRemaining -> $newRemaining")
            
            medicines[index] = medicines[index].copy(
                remaining = newRemaining,
                updatedAt = System.currentTimeMillis()
            )
            saveMedicinesToStorage(medicines)
        }
    }
    
    /** 从剂量字符串中提取数字（支持小数） */
    private fun extractDosageAmount(dosage: String): Double {
        return try {
            // 提取字符串开头的数字（支持小数）
            val matchResult = Regex("""^(\d+(?:\.\d+)?)""").find(dosage)
            val result = matchResult?.groupValues?.get(1)?.toDouble() ?: 1.0
            // 添加调试信息
            android.util.Log.d("DosageExtract", "剂量: $dosage -> 提取: $result")
            result
        } catch (e: Exception) {
            android.util.Log.e("DosageExtract", "解析失败: $dosage", e)
            1.0 // 如果解析失败，默认减1
        }
    }
    
    /** 保存药品数据到存储 */
    private fun saveMedicinesToStorage(medicines: List<Medicine>) {
        val sharedPrefs = requireContext().getSharedPreferences("medicine_data", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        val json = com.google.gson.Gson().toJson(medicines)
        editor.putString("medicines", json)
        editor.apply()
    }
    
    private fun saveCheckinRecord(checkinRecord: CheckinRecord) {
        val sharedPrefs = requireContext().getSharedPreferences("checkin_data", Context.MODE_PRIVATE)
        val existingJson = sharedPrefs.getString("checkin_records", "[]")
        val type = object : com.google.gson.reflect.TypeToken<List<CheckinRecord>>() {}.type
        val existingRecords = com.google.gson.Gson().fromJson<List<CheckinRecord>>(existingJson, type).toMutableList()
        
        // 移除同一天同一时间的旧记录
        existingRecords.removeAll { 
            it.medicineId == checkinRecord.medicineId && 
            it.time == checkinRecord.time && 
            it.date == checkinRecord.date 
        }
        
        existingRecords.add(checkinRecord)
        
        val editor = sharedPrefs.edit()
        val json = com.google.gson.Gson().toJson(existingRecords)
        editor.putString("checkin_records", json)
        editor.apply()
    }
    
    /** 跳转到药品列表 */
    private fun navigateToMedicineList() {
        val mainActivity = requireActivity()
        if (mainActivity is com.example.medicine_reminder.MainActivity) {
            val viewPager = mainActivity.findViewById<androidx.viewpager2.widget.ViewPager2>(R.id.viewPager)
            viewPager.currentItem = 1
            Toast.makeText(requireContext(), "正在跳转到药品列表...", Toast.LENGTH_SHORT).show()
        }
    }
    
    // 已删除showDetailedSchedule方法 - 不再显示提醒弹框
    
    /** 跳转到通知设置 */
    private fun navigateToNotificationSettings() {
        val mainActivity = requireActivity()
        if (mainActivity is com.example.medicine_reminder.MainActivity) {
            val viewPager = mainActivity.findViewById<androidx.viewpager2.widget.ViewPager2>(R.id.viewPager)
            viewPager.currentItem = 2
            Toast.makeText(requireContext(), "正在跳转到通知设置...", Toast.LENGTH_SHORT).show()
        }
    }
    
    /** 从存储中获取药品数据 */
    private fun getMedicinesFromStorage(): List<Medicine> {
        val sharedPrefs = requireContext().getSharedPreferences("medicine_data", Context.MODE_PRIVATE)
        val json = sharedPrefs.getString("medicines", "[]")
        val type = object : com.google.gson.reflect.TypeToken<List<Medicine>>() {}.type
        return com.google.gson.Gson().fromJson(json, type)
    }
    
    /** 从存储中获取闹钟数据 */
    private fun getAlarmsFromStorage(): List<com.example.medicine_reminder.model.MedicineReminder> {
        val sharedPrefs = requireContext().getSharedPreferences("alarm_data", Context.MODE_PRIVATE)
        val json = sharedPrefs.getString("alarms", "[]")
        val type = object : com.google.gson.reflect.TypeToken<List<com.example.medicine_reminder.model.MedicineReminder>>() {}.type
        return com.google.gson.Gson().fromJson(json, type)
    }
    
    /** 从存储中获取打卡记录数据 */
    private fun getCheckinRecordsFromStorage(): List<CheckinRecord> {
        val sharedPrefs = requireContext().getSharedPreferences("checkin_data", Context.MODE_PRIVATE)
        val json = sharedPrefs.getString("checkin_records", "[]")
        val type = object : com.google.gson.reflect.TypeToken<List<CheckinRecord>>() {}.type
        return com.google.gson.Gson().fromJson(json, type)
    }
}