package com.example.medicine_reminder.checkin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.medicine_reminder.R
import com.example.medicine_reminder.model.CheckinRecord
import com.example.medicine_reminder.model.ReminderStatus

class CheckinAdapter(
    private val records: List<CheckinRecord>
) : RecyclerView.Adapter<CheckinAdapter.CheckinViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckinViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_checkin_record, parent, false)
        return CheckinViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: CheckinViewHolder, position: Int) {
        holder.bind(records[position])
    }
    
    override fun getItemCount(): Int = records.size
    
    class CheckinViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val medicineNameText: TextView = itemView.findViewById(R.id.medicineName)
        private val dateText: TextView = itemView.findViewById(R.id.date)
        private val timeText: TextView = itemView.findViewById(R.id.time)
        private val statusText: TextView = itemView.findViewById(R.id.status)
        private val notesText: TextView = itemView.findViewById(R.id.notes)
        
        fun bind(record: CheckinRecord) {
            medicineNameText.text = record.medicineName
            dateText.text = record.date
            timeText.text = record.time
            notesText.text = record.notes
            
            when (record.status) {
                ReminderStatus.TAKEN -> {
                    statusText.text = "已服药"
                    statusText.setTextColor(ContextCompat.getColor(itemView.context, R.color.success_green))
                }
                ReminderStatus.SKIPPED -> {
                    statusText.text = "已跳过"
                    statusText.setTextColor(ContextCompat.getColor(itemView.context, R.color.warning_orange))
                }
                ReminderStatus.EXPIRED -> {
                    statusText.text = "已过期"
                    statusText.setTextColor(ContextCompat.getColor(itemView.context, R.color.error_red))
                }
                else -> {
                    statusText.text = "待服药"
                    statusText.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_secondary))
                }
            }
        }
    }
}
