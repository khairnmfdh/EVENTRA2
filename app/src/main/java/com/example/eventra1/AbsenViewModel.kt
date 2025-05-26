package com.example.eventra1.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.example.eventra1.database.DatabaseClient
import com.example.eventra1.database.DatabaseClient.Companion.getInstance
import com.example.eventra1.database.dao.DatabaseDao
import com.example.eventra1.database.DatabaseClient.Companion.getInstance
import com.example.eventra1.model.ModelDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers

class AbsenViewModel(application: Application) : AndroidViewModel(application) {
    var databaseDao: DatabaseDao? = DatabaseClient.getInstance(application)?.appDatabase?.databaseDao()

    fun addDataAbsen(
        foto: String, nama: String,
        tanggal: String, lokasi: String, keterangan: String) {
        Completable.fromAction {
            val modelDatabase = ModelDatabase()
            modelDatabase.fotoSelfie = foto
            modelDatabase.nama = nama
            modelDatabase.tanggal = tanggal
            modelDatabase.lokasi = lokasi
            modelDatabase.keterangan = keterangan
            databaseDao?.insertData(modelDatabase)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

}