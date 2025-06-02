package com.example.eventra1.model

import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Entity
import java.io.Serializable

data class FirebaseAbsensi(
    var nama: String = "",
    var fotoSelfie: String = "",
    var tanggal: String = "",
    var lokasi: String = "",
    var keterangan: String = ""
)
