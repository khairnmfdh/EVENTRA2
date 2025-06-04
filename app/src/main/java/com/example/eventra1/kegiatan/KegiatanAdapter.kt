package com.example.eventra1.kegiatan

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eventra1.R
import com.example.eventra1.model.Kegiatan

class KegiatanAdapter(
    private val listKegiatan: List<Kegiatan>,
    private val onItemClick: (Kegiatan) -> Unit
) : RecyclerView.Adapter<KegiatanAdapter.KegiatanViewHolder>() {

    inner class KegiatanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNamaKegiatan: TextView = itemView.findViewById(R.id.tvNamaKegiatan)
        val tvTanggalKegiatan: TextView = itemView.findViewById(R.id.tvTanggalKegiatan)
        val tvStatusKegiatan: TextView = itemView.findViewById(R.id.tvStatusKegiatan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KegiatanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_kegiatan, parent, false)
        return KegiatanViewHolder(view)
    }

    override fun onBindViewHolder(holder: KegiatanViewHolder, position: Int) {
        val kegiatan = listKegiatan[position]
        holder.tvNamaKegiatan.text = kegiatan.nama
        holder.tvTanggalKegiatan.text = "DATE : ${kegiatan.tanggal}"
        holder.tvStatusKegiatan.text = "STATUS : ${kegiatan.status?.uppercase()}"

        // Ganti warna teks status berdasarkan isi status
        when (kegiatan.status?.lowercase()) {
            "belum absen" -> holder.tvStatusKegiatan.setTextColor(Color.parseColor("#FF8800"))
            "hadir" -> holder.tvStatusKegiatan.setTextColor(Color.parseColor("#009688"))
            "alpha" -> holder.tvStatusKegiatan.setTextColor(Color.parseColor("#D32F2F"))
            else -> holder.tvStatusKegiatan.setTextColor(Color.BLACK)
        }

        holder.itemView.setOnClickListener { onItemClick(kegiatan) }
    }

    override fun getItemCount(): Int = listKegiatan.size
}