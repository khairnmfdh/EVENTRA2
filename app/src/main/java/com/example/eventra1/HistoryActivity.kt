package com.example.eventra1.view.history

import android.content.DialogInterface
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventra1.R
import com.example.eventra1.model.ModelDatabase
import com.example.eventra1.view.history.HistoryAdapter.HistoryAdapterCallback
import com.example.eventra1.viewmodel.HistoryViewModel

class HistoryActivity : AppCompatActivity(), HistoryAdapterCallback {
    var modelDatabaseList: MutableList<ModelDatabase> = ArrayList()
    lateinit var historyAdapter: HistoryAdapter
    lateinit var historyViewModel: HistoryViewModel

    private lateinit var binding: ActivityHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setInitLayout()
        setViewModel()
    }

    private fun setInitLayout() {
        setSupportActionBar(binding.toolbar)
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }

        tvNotFound.visibility = View.GONE

        historyAdapter = HistoryAdapter(this, modelDatabaseList, this)
        rvHistory.setHasFixedSize(true)
        rvHistory.layoutManager = LinearLayoutManager(this)
        rvHistory.adapter = historyAdapter
    }

    private fun setViewModel() {
        historyViewModel = ViewModelProviders.of(this).get(HistoryViewModel::class.java)
        historyViewModel.dataLaporan.observe(this) { modelDatabases: List<ModelDatabase> ->
            if (modelDatabases.isEmpty()) {
                tvNotFound.visibility = View.VISIBLE
                rvHistory.visibility = View.GONE
            } else {
                tvNotFound.visibility = View.GONE
                rvHistory.visibility = View.VISIBLE
            }
            historyAdapter.setDataAdapter(modelDatabases)
        }
    }

    override fun onDelete(modelDatabase: ModelDatabase?) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setMessage("Hapus riwayat ini?")
        alertDialogBuilder.setPositiveButton("Ya, Hapus") { dialogInterface, i ->
            val uid = modelDatabase!!.uid
            historyViewModel.deleteDataById(uid)
            Toast.makeText(this@HistoryActivity, "Yeay! Data yang dipilih sudah dihapus",
                Toast.LENGTH_SHORT).show()
        }
        alertDialogBuilder.setNegativeButton("Batal") { dialogInterface: DialogInterface, i:
        Int -> dialogInterface.cancel() }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}