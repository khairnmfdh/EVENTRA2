package com.example.eventra1.view.history

import ModelAbsensi
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventra1.databinding.ActivityHistoryBinding
import com.example.eventra1.view.history.HistoryAdapter.HistoryAdapterCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HistoryActivity : AppCompatActivity(), HistoryAdapter.HistoryAdapterCallback {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var historyAdapter: HistoryAdapter
    private val user = FirebaseAuth.getInstance().currentUser
    private lateinit var databaseRef: DatabaseReference
    private val absensiList = mutableListOf<ModelAbsensi>() // list untuk menampung data
    // Inisialisasi adapter secara lateinit
    private lateinit var adapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setInitLayout()
        setupRecyclerView()

        // Inisialisasi adapter di onCreate() dan set ke RecyclerView
        adapter = HistoryAdapter(this, absensiList, this)
        binding.rvHistory.adapter = adapter

        fetchAbsensiFromRealtimeDatabase()
    }

    private fun setInitLayout() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.tvNotFound.visibility = View.GONE
    }

    private fun setupRecyclerView() {
        binding.rvHistory.layoutManager = LinearLayoutManager(this)
    }

    private fun fetchAbsensiFromRealtimeDatabase() {
        databaseRef = FirebaseDatabase.getInstance().getReference("absensi")
        val currentUid = user?.uid ?: return
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                absensiList.clear() // bersihkan data lama
                for (childSnapshot in snapshot.children) {
                    val absensi = childSnapshot.getValue(ModelAbsensi::class.java)
                    if (absensi != null && absensi.uid == currentUid) {
                        absensiList.add(absensi)
                        Log.d("FirebaseData", "Jumlah data: ${absensiList.size}")
                    } else{
                        Log.d("FirebaseData", "Jumlah data: ${absensiList.size}")
                    }
                }
                // Update data ke adapter
                adapter.setData(absensiList)

                // Tampilkan pesan jika list kosong
                if (absensiList.isEmpty()) {
                    binding.tvNotFound.visibility = View.VISIBLE
                    binding.rvHistory.visibility = View.GONE
                } else {
                    binding.tvNotFound.visibility = View.GONE
                    binding.rvHistory.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HistoryActivity, "Gagal mengambil data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDelete(model: ModelAbsensi?) {
        model?.let {
            val query = databaseRef.orderByChild("timestamp").equalTo(it.timestamp)
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        child.ref.removeValue()
                    }
                    Toast.makeText(this@HistoryActivity, "Data dihapus", Toast.LENGTH_SHORT).show()
                    fetchAbsensiFromRealtimeDatabase()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@HistoryActivity, "Gagal menghapus data", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}