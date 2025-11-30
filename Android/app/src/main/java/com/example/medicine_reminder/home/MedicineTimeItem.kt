package com.example.medicine_reminder.home

import com.example.medicine_reminder.model.Medicine

data class MedicineTimeItem(
    val medicine: Medicine,
    val time: String,
    val isTaken: Boolean
)

data class ExpandedMedicineItem(
    val time: String,
    val medicines: List<MedicineTimeItem>
)
