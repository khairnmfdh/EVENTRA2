package com.example.eventra1.kegiatan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eventra1.R
import com.example.eventra1.model.Kegiatan

class TambahKegiatanAdapter(
    private val listKegiatan: List<Kegiatan>,
    private val onAddClick: (Kegiatan) -> Unit
) : RecyclerView.Adapter<TambahKegiatanAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nama: TextView = itemView.findViewById(R.id.tvNamaKegiatan)
        val tanggal: TextView = itemView.findViewById(R.id.tvTanggalKegiatan)
        val btnTambah: Button = itemView.findViewById(R.id.btnTambah)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tambah_kegiatan, parent, false)
        return ViewHolder(view)
    }



    override fun getItemCount(): Int = listKegiatan.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val kegiatan = listKegiatan[position]
        holder.nama.text = kegiatan.nama
        holder.tanggal.text = kegiatan.tanggal
        holder.btnTambah.setOnClickListener {
            onAddClick(kegiatan)
        }
    }


}


