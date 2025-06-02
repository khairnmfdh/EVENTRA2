package com.example.eventra1.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.database.FirebaseDatabase

data class FirebaseAbsensi(
    var nama: String = "",
    var fotoSelfie: String = "",
    var tanggal: String = "",
    var lokasi: String = "",
    var keterangan: String = ""
)

class AbsenViewModel(application: Application) : AndroidViewModel(application) {

    private val firebaseDb = FirebaseDatabase.getInstance()
    private val absensiRef = firebaseDb.getReference("absensi") // node utama di Realtime DB

    fun addDataAbsen(
        foto: String,
        nama: String,
        tanggal: String,
        lokasi: String,
        keterangan: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val data = FirebaseAbsensi(
            nama = nama,
            fotoSelfie = foto,
            tanggal = tanggal,
            lokasi = lokasi,
            keterangan = keterangan
        )

        val key = absensiRef.push().key
        if (key != null) {
            absensiRef.child(key).setValue(data)
                .addOnSuccessListener {
                    Log.d("AbsenViewModel", "Data berhasil disimpan ke Firebase")
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Log.e("AbsenViewModel", "Gagal menyimpan ke Firebase: ${e.message}")
                    onFailure(e)
                }
        } else {
            onFailure(Exception("Key Firebase null"))
        }
    }
}
