package com.example.eventra1.view.history

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eventra1.R
import ModelAbsensi

class HistoryAdapter(
    private val context: Context,
    private var data: List<ModelAbsensi>,
    private val callback: HistoryAdapterCallback
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    interface HistoryAdapterCallback {
        fun onDelete(model: ModelAbsensi?)
        // Tambahkan fungsi lain jika dibutuhkan, misalnya onClick
    }

    fun setData(newData: List<ModelAbsensi>) {
        this.data = newData
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvLokasi: TextView = itemView.findViewById(R.id.tvLokasi)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_history_absen, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val absen = data[position]

        holder.tvStatus.text = absen.status
        holder.tvLokasi.text = absen.lokasi
        holder.tvTimestamp.text = absen.timestamp

        // Tambahkan aksi long click untuk hapus
        holder.itemView.setOnLongClickListener {
            callback.onDelete(absen)
            true
        }

        // Jika ingin menambahkan klik biasa, bisa tambahkan di sini
        // holder.itemView.setOnClickListener { ... }
    }

    override fun getItemCount(): Int = data.size
}