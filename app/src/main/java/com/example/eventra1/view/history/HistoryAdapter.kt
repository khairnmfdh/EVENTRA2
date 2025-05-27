package com.example.eventra1.view.history

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.eventra1.R
import com.example.eventra1.model.ModelDatabase
import com.example.eventra1.utils.BitmapManager.base64ToBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.imageview.ShapeableImageView
import java.lang.String
import kotlin.Int

class HistoryAdapter(
    var mContext: Context,
    var modelDatabase: MutableList<ModelDatabase>,
    var mAdapterCallback: HistoryAdapterCallback
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    fun setDataAdapter(items: List<ModelDatabase>) {
        modelDatabase.clear()
        modelDatabase.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_history_absen, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = modelDatabase[position]
        holder.tvNomor.text = String.valueOf(data.uid)
        holder.tvNama.text = data.nama
        holder.tvLokasi.text = data.lokasi
        holder.tvAbsenTime.text = data.tanggal
        holder.tvStatusAbsen.text = data.keterangan

        Glide.with(mContext)
            .load(base64ToBitmap(data.fotoSelfie))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.ic_photo_camera)
            .into(holder.imageProfile)

        when (data.keterangan) {
            "Absen Masuk" -> {
                holder.colorStatus.setBackgroundResource(R.drawable.bg_circle_radius)
                holder.colorStatus.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
            }
            "Absen Keluar" -> {
                holder.colorStatus.setBackgroundResource(R.drawable.bg_circle_radius)
                holder.colorStatus.backgroundTintList = ColorStateList.valueOf(Color.RED)
            }
            "Izin" -> {
                holder.colorStatus.setBackgroundResource(R.drawable.bg_circle_radius)
                holder.colorStatus.backgroundTintList = ColorStateList.valueOf(Color.BLUE)
            }
        }
    }

    override fun getItemCount(): Int {
        return modelDatabase.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvStatusAbsen: TextView = itemView.findViewById(R.id.tvStatusAbsen)
        val tvNomor: TextView = itemView.findViewById(R.id.tvNomor)
        val tvNama: TextView = itemView.findViewById(R.id.tvNama)
        val tvLokasi: TextView = itemView.findViewById(R.id.tvLokasi)
        val tvAbsenTime: TextView = itemView.findViewById(R.id.tvAbsenTime)
        val cvHistory: CardView = itemView.findViewById(R.id.cvHistory)
        val imageProfile: ShapeableImageView = itemView.findViewById(R.id.imageProfile)
        val colorStatus: View = itemView.findViewById(R.id.colorStatus)

        init {
            cvHistory.setOnClickListener {
                val modelLaundry = modelDatabase[adapterPosition]
                mAdapterCallback.onDelete(modelLaundry)
            }
        }
    }

    interface HistoryAdapterCallback {
        fun onDelete(modelDatabase: ModelDatabase?)
    }
}