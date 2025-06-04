package com.example.eventra1.view.main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventra1.view.profile.ProfileActivity
import com.example.eventra1.databinding.ActivityMainBinding
import com.example.eventra1.kegiatan.KegiatanAdapter
import com.example.eventra1.kegiatan.TambahKegiatanActivity
import com.example.eventra1.model.Kegiatan
import com.example.eventra1.view.absen.AbsenActivity
import com.example.eventra1.view.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var kegiatanAdapter: KegiatanAdapter
    private val kegiatanList = mutableListOf<Kegiatan>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tampilkanNamaUser()
        setupRecyclerView()
        loadKegiatanUser()

        binding.imageProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.btnTambahKegiatan.setOnClickListener {
            val intent = Intent(this, TambahKegiatanActivity::class.java)
            startActivity(intent)
            finish()
        }

        /*binding.btnTambahKegiatan.setOnClickListener {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            val kegiatanBaru = Kegiatan("Kegiatan Baru User", "Deskripsi")
            FirebaseDatabase.getInstance().getReference("kegiatan").push().setValue(kegiatanBaru)
                .addOnSuccessListener {
                    Toast.makeText(this, "Kegiatan ditambahkan", Toast.LENGTH_SHORT).show()
                    loadKegiatanFromFirebase()
                }
        }*/

    }

    private fun tampilkanNamaUser() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val databaseRef = FirebaseDatabase.getInstance().getReference("profile").child(uid)
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nama = snapshot.child("nama").getValue(String::class.java)
                if (nama != null) {
                    binding.textViewNamaDisplay.text = "Hello, $nama!"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Gagal ambil data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupRecyclerView() {
        kegiatanAdapter = KegiatanAdapter(kegiatanList) { kegiatan ->
            val namaKegiatan = kegiatan.nama
            if (!namaKegiatan.isNullOrEmpty()) {
                bukaActivityAbsen(namaKegiatan)
            } else {
                Toast.makeText(this, "Nama kegiatan tidak tersedia", Toast.LENGTH_SHORT).show()
            }
        }
        binding.recyclerViewKegiatan.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewKegiatan.adapter = kegiatanAdapter
    }


    private fun loadKegiatanUser() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().getReference("kegiatan_user").child(uid)
        val umumRef = FirebaseDatabase.getInstance().getReference("kegiatan_umum")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val kegiatanIdList = snapshot.children.mapNotNull { it.key }

                if (kegiatanIdList.isEmpty()) {
                    Toast.makeText(this@MainActivity, "Tidak ada ID kegiatan ditemukan", Toast.LENGTH_SHORT).show()
                }

                umumRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(allSnapshot: DataSnapshot) {
                        kegiatanList.clear()
                        for (id in kegiatanIdList) {
                            val kegiatanSnapshot = allSnapshot.child(id)
                            val kegiatan = kegiatanSnapshot.getValue(Kegiatan::class.java)
                            kegiatan?.id = id
                            if (kegiatan != null) {
                                kegiatanList.add(kegiatan)
                                println("Ditemukan kegiatan: ${kegiatan.nama}") // Logging
                            } else {
                                println("Kegiatan ID $id tidak ditemukan di kegiatan_umum")
                            }
                        }

                        println("Total kegiatan yang dimuat: ${kegiatanList.size}")
                        kegiatanAdapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@MainActivity, "Gagal ambil kegiatan_umum", Toast.LENGTH_SHORT).show()
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Gagal ambil kegiatan_user", Toast.LENGTH_SHORT).show()
            }
        })
    }


    override fun onResume() {
        super.onResume()
        loadKegiatanUser()
    }





    private fun bukaActivityAbsen(namaKegiatan: String) {
        val intent = Intent(this, AbsenActivity::class.java)
        intent.putExtra("nama_kegiatan", namaKegiatan)
        startActivity(intent)
    }


    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
