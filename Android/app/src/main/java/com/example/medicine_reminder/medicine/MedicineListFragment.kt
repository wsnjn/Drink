package com.example.medicine_reminder.medicine

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medicine_reminder.R
import com.example.medicine_reminder.model.Medicine
import com.example.medicine_reminder.model.CheckinRecord
import com.example.medicine_reminder.model.ReminderStatus
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class MedicineListFragment : Fragment() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MedicineAdapter
    private lateinit var fabAdd: FloatingActionButton
    private val medicines = mutableListOf<Medicine>()
    private var sharedPreferencesListener: SharedPreferences.OnSharedPreferenceChangeListener? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_medicine_list, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.recyclerView)
        fabAdd = view.findViewById(R.id.fabAdd)
        
        setupRecyclerView()
        loadMedicines()
        
        fabAdd.setOnClickListener {
            showAddMedicineDialog()
        }
    }
    
    override fun onResume() {
        super.onResume()
        // 每次显示时刷新数据，确保剩余数量是最新的
        loadMedicines()
        
        // 注册SharedPreferences监听器，实时监听数据变化
        val sharedPrefs = requireContext().getSharedPreferences("medicine_data", Context.MODE_PRIVATE)
        sharedPreferencesListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "medicines") {
                // 药品数据发生变化时，立即刷新列表
                loadMedicines()
            }
        }
        sharedPrefs.registerOnSharedPreferenceChangeListener(sharedPreferencesListener)
    }
    
    override fun onPause() {
        super.onPause()
        // 注销SharedPreferences监听器
        val sharedPrefs = requireContext().getSharedPreferences("medicine_data", Context.MODE_PRIVATE)
        sharedPreferencesListener?.let { listener ->
            sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
    
    private fun setupRecyclerView() {
        adapter = MedicineAdapter(medicines, 
            onItemClick = { medicine -> showMedicineOptionsDialog(medicine) },
            onItemLongClick = { medicine -> showCheckinDialog(medicine) }
        )
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }
    
    private fun loadMedicines() {
        medicines.clear()
        medicines.addAll(getMedicinesFromStorage())
        adapter.notifyDataSetChanged()
    }
    
    private fun showAddMedicineDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_medicine, null)
        
        val etName = dialogView.findViewById<EditText>(R.id.etMedicineName)
        val etDosage = dialogView.findViewById<EditText>(R.id.etMedicineDosage)
        val etFrequency = dialogView.findViewById<EditText>(R.id.etMedicineFrequency)
        val etRemaining = dialogView.findViewById<EditText>(R.id.etMedicineRemaining)
        val etTotal = dialogView.findViewById<EditText>(R.id.etMedicineTotal)
        val btnSelectTimes = dialogView.findViewById<Button>(R.id.btnSelectTimes)
        val tvSelectedTimes = dialogView.findViewById<TextView>(R.id.tvSelectedTimes)
        val btnSelectStartDate = dialogView.findViewById<Button>(R.id.btnSelectStartDate)
        val tvSelectedStartDate = dialogView.findViewById<TextView>(R.id.tvSelectedStartDate)
        val btnSelectEndDate = dialogView.findViewById<Button>(R.id.btnSelectEndDate)
        val tvSelectedEndDate = dialogView.findViewById<TextView>(R.id.tvSelectedEndDate)
        
        var selectedTimes = mutableListOf<String>()
        var selectedStartDate = System.currentTimeMillis()
        var selectedEndDate: Long? = null
        
        btnSelectTimes.setOnClickListener {
            showTimeSelectionDialog(emptyList()) { times ->
                selectedTimes = times.toMutableList()
                tvSelectedTimes.text = "已选择时间: ${times.joinToString(", ")}"
            }
        }
        
        btnSelectStartDate.setOnClickListener {
            showDatePickerDialog { date ->
                selectedStartDate = date
                tvSelectedStartDate.text = "开始日期: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(date))}"
            }
        }
        
        btnSelectEndDate.setOnClickListener {
            showDatePickerDialog { date ->
                selectedEndDate = date
                tvSelectedEndDate.text = "结束日期: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(date))}"
            }
        }
        
        val dialog = AlertDialog.Builder(context)
            .setTitle("添加药品")
            .setView(dialogView)
            .setPositiveButton("确定") { _, _ ->
                if (validateAndAddMedicine(
                    etName.text.toString(),
                    etDosage.text.toString(),
                    etFrequency.text.toString(),
                    etRemaining.text.toString(),
                    etTotal.text.toString(),
                    selectedTimes,
                    selectedStartDate,
                    selectedEndDate
                )) {
                    loadMedicines()
                    Toast.makeText(context, "药品添加成功", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消") { _, _ -> }
            .create()
        
        dialog.show()
    }
    
    private fun validateAndAddMedicine(
        name: String,
        dosage: String,
        frequencyStr: String,
        remainingStr: String,
        totalStr: String,
        times: List<String>,
        startDate: Long,
        endDate: Long?
    ): Boolean {
        if (name.isBlank()) {
            Toast.makeText(context, "请输入药品名称", Toast.LENGTH_SHORT).show()
            return false
        }
        if (dosage.isBlank()) {
            Toast.makeText(context, "请输入剂量", Toast.LENGTH_SHORT).show()
            return false
        }
        if (times.isEmpty()) {
            Toast.makeText(context, "请选择服用时间", Toast.LENGTH_SHORT).show()
            return false
        }
        
        val frequency = frequencyStr.toIntOrNull() ?: times.size
        val remaining = remainingStr.toDoubleOrNull() ?: 0.0
        val total = totalStr.toIntOrNull() ?: remaining
        
        val medicine = Medicine(
            name = name,
            dosage = dosage,
            frequency = frequency,
            times = times,
            remaining = remaining,
            unit = extractUnit(dosage),
            notes = "",
            isActive = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        medicines.add(medicine)
        saveMedicinesToStorage()
        return true
    }
    
    private fun extractUnit(dosage: String): String {
        return when {
            dosage.contains("片") -> "片"
            dosage.contains("粒") -> "粒"
            dosage.contains("ml") -> "ml"
            dosage.contains("mg") -> "mg"
            dosage.contains("g") -> "g"
            else -> "片"
        }
    }
    
    private fun showTimeSelectionDialog(selectedTimes: List<String>, onTimesSelected: (List<String>) -> Unit) {
        // 生成包含30分钟选项的时间列表
        val timeOptions = mutableListOf<String>()
        
        // 生成00:00到23:59的所有时间，每30分钟一个
        for (hour in 0..23) {
            for (minute in listOf(0, 30)) {
                val timeString = String.format("%02d:%02d", hour, minute)
                timeOptions.add(timeString)
            }
        }
        
        val selectedItems = BooleanArray(timeOptions.size)
        
        // 预选已选择的时间
        selectedTimes.forEach { selectedTime ->
            val index = timeOptions.indexOf(selectedTime)
            if (index != -1) {
                selectedItems[index] = true
            }
        }
        
        AlertDialog.Builder(context)
            .setTitle("选择服用时间（支持多选）")
            .setMultiChoiceItems(timeOptions.toTypedArray(), selectedItems) { _, which, isChecked ->
                selectedItems[which] = isChecked
            }
            .setPositiveButton("确定") { _, _ ->
                val newSelectedTimes = mutableListOf<String>()
                for (i in selectedItems.indices) {
                    if (selectedItems[i]) {
                        newSelectedTimes.add(timeOptions[i])
                    }
                }
                onTimesSelected(newSelectedTimes)
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun showDatePickerDialog(onDateSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth)
                onDateSelected(selectedCalendar.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
    
    private fun showMedicineOptionsDialog(medicine: Medicine) {
        val options = arrayOf("查看详情", "编辑药品", "删除药品")
        
        AlertDialog.Builder(context)
            .setTitle("药品操作")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showMedicineDetails(medicine)
                    1 -> showEditMedicineDialog(medicine)
                    2 -> showDeleteConfirmDialog(medicine)
                }
            }
            .show()
    }
    
    private fun showMedicineDetails(medicine: Medicine) {
        val message = """
            药品名称: ${medicine.name}
            剂量: ${medicine.dosage}
            服用频率: 每天${medicine.frequency}次
            服用时间: ${medicine.times.joinToString(", ")}
            剩余数量: ${medicine.remaining}${medicine.unit}
            备注: ${medicine.notes}
            创建时间: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(medicine.createdAt))}
        """.trimIndent()
        
        AlertDialog.Builder(context)
            .setTitle("药品详情")
            .setMessage(message)
            .setPositiveButton("确定", null)
            .show()
    }
    
    private fun showEditMedicineDialog(medicine: Medicine) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_medicine, null)
        
        val etName = dialogView.findViewById<EditText>(R.id.etMedicineName)
        val etDosage = dialogView.findViewById<EditText>(R.id.etMedicineDosage)
        val etFrequency = dialogView.findViewById<EditText>(R.id.etMedicineFrequency)
        val etRemaining = dialogView.findViewById<EditText>(R.id.etMedicineRemaining)
        val etTotal = dialogView.findViewById<EditText>(R.id.etMedicineTotal)
        val btnSelectTimes = dialogView.findViewById<Button>(R.id.btnSelectTimes)
        val tvSelectedTimes = dialogView.findViewById<TextView>(R.id.tvSelectedTimes)
        val btnSelectStartDate = dialogView.findViewById<Button>(R.id.btnSelectStartDate)
        val tvSelectedStartDate = dialogView.findViewById<TextView>(R.id.tvSelectedStartDate)
        val btnSelectEndDate = dialogView.findViewById<Button>(R.id.btnSelectEndDate)
        val tvSelectedEndDate = dialogView.findViewById<TextView>(R.id.tvSelectedEndDate)
        
        // 填充现有数据
        etName.setText(medicine.name)
        etDosage.setText(medicine.dosage)
        etFrequency.setText(medicine.frequency.toString())
        etRemaining.setText(medicine.remaining.toString())
        etTotal.setText((medicine.remaining + 50).toString()) // 假设总数量
        
        var selectedTimes = medicine.times.toMutableList()
        var selectedStartDate = medicine.createdAt
        var selectedEndDate: Long? = null
        
        tvSelectedTimes.text = "已选择时间: ${medicine.times.joinToString(", ")}"
        tvSelectedStartDate.text = "开始日期: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(medicine.createdAt))}"
        tvSelectedEndDate.text = "结束日期: 无"
        
        btnSelectTimes.setOnClickListener {
            showTimeSelectionDialog(selectedTimes) { times ->
                selectedTimes = times.toMutableList()
                tvSelectedTimes.text = "已选择时间: ${times.joinToString(", ")}"
            }
        }
        
        btnSelectStartDate.setOnClickListener {
            showDatePickerDialog { date ->
                selectedStartDate = date
                tvSelectedStartDate.text = "开始日期: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(date))}"
            }
        }
        
        btnSelectEndDate.setOnClickListener {
            showDatePickerDialog { date ->
                selectedEndDate = date
                tvSelectedEndDate.text = "结束日期: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(date))}"
            }
        }
        
        val dialog = AlertDialog.Builder(context)
            .setTitle("编辑药品")
            .setView(dialogView)
            .setPositiveButton("保存") { _, _ ->
                if (validateAndUpdateMedicine(
                    medicine,
                    etName.text.toString(),
                    etDosage.text.toString(),
                    etFrequency.text.toString(),
                    etRemaining.text.toString(),
                    etTotal.text.toString(),
                    selectedTimes,
                    selectedStartDate,
                    selectedEndDate
                )) {
                    loadMedicines()
                    Toast.makeText(context, "药品更新成功", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消") { _, _ -> }
            .create()
        
        dialog.show()
    }
    
    private fun validateAndUpdateMedicine(
        originalMedicine: Medicine,
        name: String,
        dosage: String,
        frequencyStr: String,
        remainingStr: String,
        totalStr: String,
        times: List<String>,
        startDate: Long,
        endDate: Long?
    ): Boolean {
        if (name.isBlank()) {
            Toast.makeText(context, "请输入药品名称", Toast.LENGTH_SHORT).show()
            return false
        }
        if (dosage.isBlank()) {
            Toast.makeText(context, "请输入剂量", Toast.LENGTH_SHORT).show()
            return false
        }
        if (times.isEmpty()) {
            Toast.makeText(context, "请选择服用时间", Toast.LENGTH_SHORT).show()
            return false
        }
        
        val frequency = frequencyStr.toIntOrNull() ?: times.size
        val remaining = remainingStr.toDoubleOrNull() ?: 0.0
        val total = totalStr.toIntOrNull() ?: remaining
        
        // 更新药品信息
        val index = medicines.indexOf(originalMedicine)
        if (index != -1) {
            medicines[index] = originalMedicine.copy(
                name = name,
                dosage = dosage,
                frequency = frequency,
                times = times,
                remaining = remaining,
                unit = extractUnit(dosage),
                updatedAt = System.currentTimeMillis()
            )
            saveMedicinesToStorage()
            return true
        }
        return false
    }
    
    private fun showDeleteConfirmDialog(medicine: Medicine) {
        AlertDialog.Builder(context)
            .setTitle("删除药品")
            .setMessage("确定要删除药品 \"${medicine.name}\" 吗？此操作不可撤销。")
            .setPositiveButton("删除") { _, _ ->
                medicines.remove(medicine)
                saveMedicinesToStorage()
                loadMedicines()
                Toast.makeText(context, "药品已删除", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun showCheckinDialog(medicine: Medicine) {
        val times = medicine.times
        val timeOptions = times.toTypedArray()

        AlertDialog.Builder(context)
            .setTitle("打卡 - ${medicine.name}")
            .setItems(timeOptions) { _, which ->
                val selectedTime = times[which]
                val checkinRecord = CheckinRecord(
                    medicineId = medicine.id,
                    medicineName = medicine.name,
                    date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                    time = selectedTime,
                    status = ReminderStatus.TAKEN,
                    timestamp = System.currentTimeMillis()
                )
                saveCheckinRecord(checkinRecord)
                
                // 减少药品剩余数量
                decreaseMedicineRemaining(medicine)
                
                Toast.makeText(context, "✅ 已打卡: ${medicine.name} - $selectedTime", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    /** 减少药品剩余数量 */
    private fun decreaseMedicineRemaining(medicine: Medicine) {
        val index = medicines.indexOfFirst { it.id == medicine.id }
        
        if (index != -1 && medicines[index].remaining > 0) {
            // 解析剂量中的数字部分（支持小数）
            val dosageAmount = extractDosageAmount(medicine.dosage)
            val newRemaining = maxOf(0.0, medicines[index].remaining - dosageAmount)
            
            medicines[index] = medicines[index].copy(
                remaining = newRemaining,
                updatedAt = System.currentTimeMillis()
            )
            saveMedicinesToStorage()
            adapter.notifyItemChanged(index) // 刷新特定项目
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
    
    private fun getMedicinesFromStorage(): List<Medicine> {
        val sharedPrefs = requireContext().getSharedPreferences("medicine_data", Context.MODE_PRIVATE)
        val json = sharedPrefs.getString("medicines", "[]")
        val type = object : TypeToken<List<Medicine>>() {}.type
        return Gson().fromJson(json, type)
    }
    
    private fun saveMedicinesToStorage() {
        val sharedPrefs = requireContext().getSharedPreferences("medicine_data", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        val json = Gson().toJson(medicines)
        editor.putString("medicines", json)
        editor.apply()
    }
}
