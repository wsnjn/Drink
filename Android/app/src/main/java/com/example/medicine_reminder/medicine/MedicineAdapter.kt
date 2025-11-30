package com.example.medicine_reminder.medicine

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.medicine_reminder.R
import com.example.medicine_reminder.model.Medicine

class MedicineAdapter(
    private val medicines: MutableList<Medicine>,
    private val onItemClick: (Medicine) -> Unit,
    private val onItemLongClick: (Medicine) -> Unit = {}
) : RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medicine, parent, false)
        return MedicineViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: MedicineViewHolder, position: Int) {
        val medicine = medicines[position]
        holder.bind(medicine)
        holder.itemView.setOnClickListener { onItemClick(medicine) }
        holder.itemView.setOnLongClickListener { 
            onItemLongClick(medicine)
            true
        }
    }
    
    override fun getItemCount(): Int = medicines.size
    
    class MedicineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.medicineName)
        private val dosageText: TextView = itemView.findViewById(R.id.medicineDosage)
        private val remainingText: TextView = itemView.findViewById(R.id.medicineRemaining)
        private val frequencyText: TextView = itemView.findViewById(R.id.medicineFrequency)
        
        fun bind(medicine: Medicine) {
            nameText.text = medicine.name
            dosageText.text = "剂量: ${medicine.dosage}"
            remainingText.text = "剩余: ${formatRemaining(medicine.remaining)}${medicine.unit}"
            frequencyText.text = "每天${medicine.frequency}次"
        }
        
        private fun formatRemaining(remaining: Double): String {
            return if (remaining == remaining.toInt().toDouble()) {
                remaining.toInt().toString()
            } else {
                String.format("%.1f", remaining)
            }
        }
    }
}
