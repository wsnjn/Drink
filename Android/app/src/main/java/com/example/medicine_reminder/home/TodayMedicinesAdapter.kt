package com.example.medicine_reminder.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.medicine_reminder.R
import com.example.medicine_reminder.model.Medicine

class TodayMedicinesAdapter(
    private val onTimeSlotClick: (String, List<MedicineTimeItem>) -> Unit
) : RecyclerView.Adapter<TodayMedicinesAdapter.ViewHolder>() {

    private var expandedItems = mutableListOf<ExpandedMedicineItem>()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvMedicines: TextView = itemView.findViewById(R.id.tvMedicines)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_today_medicine_time, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = expandedItems[position]
        holder.tvTime.text = item.time
        
        val medicineText = item.medicines.joinToString("\n") { medicineItem ->
            val status = if (medicineItem.isTaken) "✅" else "⏰"
            "$status ${medicineItem.medicine.name} (${medicineItem.medicine.dosage})"
        }
        holder.tvMedicines.text = medicineText
        
        holder.itemView.setOnClickListener {
            // 点击时间段 - 处理该时间段的所有药品
            onTimeSlotClick(item.time, item.medicines)
        }
    }

    override fun getItemCount(): Int = expandedItems.size

    fun updateData(newItems: List<ExpandedMedicineItem>) {
        expandedItems.clear()
        expandedItems.addAll(newItems)
        notifyDataSetChanged()
    }
}
