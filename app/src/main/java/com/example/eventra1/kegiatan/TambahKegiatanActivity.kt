package com.example.eventra1.kegiatan

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eventra1.R
import com.example.eventra1.kegiatan.TambahKegiatanAdapter
import com.example.eventra1.model.Kegiatan
import com.example.eventra1.view.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TambahKegiatanActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TambahKegiatanAdapter
    private val listKegiatanUmum = mutableListOf<Kegiatan>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_kegiatan)

/*       val ref = FirebaseDatabase.getInstance().getReference("kegiatan_umum")
        val batchUpdate = mutableMapOf<String, Any>()

        val daftarKegiatanBaru = listOf(
            Kegiatan("Seminar AI dan Etika", "2025-06-10", ""),
            Kegiatan("Workshop Keamanan Siber", "2025-06-12", ""),
            Kegiatan("Pelatihan Data Science Dasar", "2025-06-14", ""),
            Kegiatan("Bootcamp Mobile Development", "2025-06-16", ""),
            Kegiatan("Seminar Teknologi Blockchain", "2025-06-18", ""),
            Kegiatan("Webinar Cloud Computing", "2025-06-19", ""),
            Kegiatan("Pelatihan UI/UX Design", "2025-06-20", ""),
            Kegiatan("Hackathon Smart City", "2025-06-22", ""),
            Kegiatan("Workshop DevOps dan CI/CD", "2025-06-24", ""),
            Kegiatan("Seminar Tren Teknologi", "2025-06-26", "")
        )

        for (kegiatan_umum in daftarKegiatanBaru) {
            val key = ref.push().key
            if (key != null) {
                batchUpdate[key] = kegiatan_umum
            }
        }
        ref.updateChildren(batchUpdate)*/

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            val intent = Intent(this@TambahKegiatanActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        recyclerView = findViewById(R.id.recyclerViewTambahKegiatan)
        recyclerView.layoutManager = LinearLayoutManager(this)



        adapter = TambahKegiatanAdapter(listKegiatanUmum) { kegiatan ->
            tambahKegiatanKeUser(kegiatan)
        }

        recyclerView.adapter = adapter

        loadKegiatanUmum()
    }


    private fun loadKegiatanUmum() {
        val refSemuaKegiatan = FirebaseDatabase.getInstance().getReference("kegiatan_umum")

        refSemuaKegiatan.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listKegiatanUmum.clear()

                for (data in snapshot.children) {
                    val kegiatan = data.getValue(Kegiatan::class.java)
                    if (kegiatan != null) {
                        kegiatan.id = data.key // kita simpan ID-nya untuk referensi nanti
                        listKegiatanUmum.add(kegiatan)
                    }
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TambahKegiatanActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun tambahKegiatanKeUser(kegiatan: Kegiatan) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val kegiatanId = kegiatan.id ?: return

        val ref = FirebaseDatabase.getInstance().getReference("kegiatan_user").child(uid).child(kegiatanId)

        ref.setValue(true)
            .addOnSuccessListener {
                Toast.makeText(this, "Kegiatan berhasil ditambahkan", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menambahkan kegiatan", Toast.LENGTH_SHORT).show()
            }
    }

}
