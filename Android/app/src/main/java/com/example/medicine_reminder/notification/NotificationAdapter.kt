package com.example.medicine_reminder.notification

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.medicine_reminder.R
import com.example.medicine_reminder.model.NotificationSettings

class NotificationAdapter(
    private val notifications: MutableList<NotificationSettings>,
    private val onItemClick: (NotificationSettings) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNotificationTime: TextView = itemView.findViewById(R.id.tvNotificationTime)
        val tvNotificationName: TextView = itemView.findViewById(R.id.tvNotificationName)
        val tvNotificationDays: TextView = itemView.findViewById(R.id.tvNotificationDays)
        val switchNotificationToggle: Switch = itemView.findViewById(R.id.switchNotificationToggle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.tvNotificationTime.text = notification.time
        holder.tvNotificationName.text = "通知: ${notification.name}"
        holder.tvNotificationDays.text = "重复: ${getDaysString(notification.daysOfWeek)}"
        holder.switchNotificationToggle.isChecked = notification.isEnabled
        holder.itemView.setOnClickListener { onItemClick(notification) }

        holder.switchNotificationToggle.setOnCheckedChangeListener { _, isChecked ->
            notification.isEnabled = isChecked
            // TODO: 更新通知调度
        }
    }

    override fun getItemCount(): Int = notifications.size

    private fun getDaysString(days: List<Int>): String {
        if (days.size == 7) return "每天"
        if (days.isEmpty()) return "不重复"
        val dayNames = listOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")
        return days.sorted().joinToString(", ") { dayNames[it - 1] }
    }
}
