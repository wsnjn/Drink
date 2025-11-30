package com.example.medicine_reminder.checkin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medicine_reminder.R
import com.example.medicine_reminder.model.CheckinRecord
import com.example.medicine_reminder.model.ReminderStatus
import java.text.SimpleDateFormat
import java.util.*

class CheckinHistoryFragment : Fragment() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CheckinAdapter
    private val checkinRecords = mutableListOf<CheckinRecord>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_checkin_history, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.recyclerView)
        
        setupRecyclerView()
        loadCheckinRecords()
    }
    
    private fun setupRecyclerView() {
        adapter = CheckinAdapter(checkinRecords)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }
    
    private fun loadCheckinRecords() {
        checkinRecords.clear()
        checkinRecords.addAll(getCheckinRecordsFromStorage())
        adapter.notifyDataSetChanged()
    }
    
    private fun getCheckinRecordsFromStorage(): List<CheckinRecord> {
        // 从存储中获取打卡记录
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
            Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }.time
        )
        
        return listOf(
            CheckinRecord(
                medicineId = "1",
                medicineName = "维生素C",
                date = today,
                time = "08:00",
                status = ReminderStatus.TAKEN,
                notes = "按时服用"
            ),
            CheckinRecord(
                medicineId = "2",
                medicineName = "感冒药",
                date = today,
                time = "08:00",
                status = ReminderStatus.TAKEN,
                notes = "饭后服用"
            ),
            CheckinRecord(
                medicineId = "2",
                medicineName = "感冒药",
                date = today,
                time = "14:00",
                status = ReminderStatus.SKIPPED,
                notes = "忘记服用"
            ),
            CheckinRecord(
                medicineId = "1",
                medicineName = "维生素C",
                date = yesterday,
                time = "08:00",
                status = ReminderStatus.TAKEN,
                notes = "按时服用"
            ),
            CheckinRecord(
                medicineId = "2",
                medicineName = "感冒药",
                date = yesterday,
                time = "08:00",
                status = ReminderStatus.TAKEN,
                notes = "饭后服用"
            ),
            CheckinRecord(
                medicineId = "2",
                medicineName = "感冒药",
                date = yesterday,
                time = "14:00",
                status = ReminderStatus.TAKEN,
                notes = "按时服用"
            ),
            CheckinRecord(
                medicineId = "2",
                medicineName = "感冒药",
                date = yesterday,
                time = "20:00",
                status = ReminderStatus.TAKEN,
                notes = "按时服用"
            )
        )
    }
}
