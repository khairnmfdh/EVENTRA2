package com.example.eventra1.view.absen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.eventra1.view.profile.ProfileActivity
import com.example.eventra1.R
import com.example.eventra1.view.main.MainActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class AbsenActivity : AppCompatActivity() {

    private lateinit var btnHadir: Button
    private lateinit var btnIzin: Button
    private lateinit var btnSakit: Button
    private lateinit var btnSubmit: MaterialButton
    private lateinit var inputLokasi: EditText
    private var selectedStatus: String? = null
    private var imageUri: Uri? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val storageRef = FirebaseStorage.getInstance().reference
    private val firestore = FirebaseFirestore.getInstance()
    private val user = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_absen)

        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val kegiatanId = intent.getStringExtra("id_kegiatan")

        if (kegiatanId != null) {
            val ref = FirebaseDatabase.getInstance().getReference("kegiatan_umum").child(kegiatanId)
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val namaKegiatan = snapshot.child("nama").getValue(String::class.java)
                    if (namaKegiatan != null) {
                        tvTitle.text = namaKegiatan
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@AbsenActivity, "Gagal ambil nama kegiatan", Toast.LENGTH_SHORT).show()
                }
            })
        }


        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            val intent = Intent(this@AbsenActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Ambil dan tampilkan data profile dari Firebase Realtime Database
        val tvNama = findViewById<TextView>(R.id.tvNama)
        val tvProdi = findViewById<TextView>(R.id.tvProdi)
        val tvFakultas = findViewById<TextView>(R.id.tvFakultas)
        val tvUniversitas = findViewById<TextView>(R.id.tvUniversitas)





        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val profileRef = FirebaseDatabase.getInstance().getReference("profile").child(uid)
            profileRef.addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    val nama = snapshot.child("nama").getValue(String::class.java)
                    val prodi = snapshot.child("prodi").getValue(String::class.java)
                    val fakultas = snapshot.child("fakultas").getValue(String::class.java)
                    val universitas = snapshot.child("universitas").getValue(String::class.java)

                    if (nama != null) tvNama.text = nama
                    if (prodi != null) tvProdi.text = prodi
                    if (fakultas != null) tvFakultas.text = fakultas
                    if (universitas != null) tvUniversitas.text = universitas
                }

                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    Toast.makeText(this@AbsenActivity, "Gagal mengambil data profil", Toast.LENGTH_SHORT).show()
                }
            })
        }


        val imageProfile = findViewById<ImageView>(R.id.imageProfile)

        imageProfile.setOnClickListener {
            val intent = Intent(this@AbsenActivity, ProfileActivity::class.java)
            startActivity(intent)
        }

        // Init
        btnHadir = findViewById(R.id.btnHadir)
        btnIzin = findViewById(R.id.btnIzin)
        btnSakit = findViewById(R.id.btnSakit)
        btnSubmit = findViewById(R.id.btnAbsen)
        inputLokasi = findViewById(R.id.inputLokasi)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Button status logic
        val allButtons = listOf(btnHadir, btnIzin, btnSakit)
        val statusMap = mapOf(
            btnHadir to "HADIR",
            btnIzin to "IZIN",
            btnSakit to "SAKIT"
        )

        allButtons.forEach { button ->
            button.setOnClickListener {
                selectedStatus = statusMap[button]
                allButtons.forEach { it.setBackgroundTintList(getColorStateList(R.color.gray)) }
                button.setBackgroundTintList(getColorStateList(R.color.teal_200))
            }
        }

        // Submit logic
        btnSubmit.setOnClickListener {
            if (selectedStatus == null) {
                Toast.makeText(this, "Pilih status terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            getCurrentLocation { location ->
                val latLng = "${location.latitude},${location.longitude}"
                inputLokasi.setText(latLng)

                uploadImageToFirebase { imageUrl ->
                    saveAbsenToFirestore(imageUrl, latLng)
                }
            }
        }
    }

    private fun getCurrentLocation(callback: (Location) -> Unit) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                callback(location)
            } else {
                Toast.makeText(this, "Gagal mendapatkan lokasi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadImageToFirebase(onComplete: (String) -> Unit) {
        val fileName = "absensi/${UUID.randomUUID()}.jpg"
        val fileRef = storageRef.child(fileName)

        imageUri?.let {
            fileRef.putFile(it).addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    onComplete(uri.toString())
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Gagal upload gambar", Toast.LENGTH_SHORT).show()
            }
        } ?: onComplete("") // Jika tidak ada gambar, tetap lanjut
    }

    fun ambilDataKegiatan() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val kegiatanUserRef = FirebaseDatabase.getInstance().getReference("kegiatan_user").child(userId!!)

        kegiatanUserRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (kegiatanSnapshot in snapshot.children) {
                    val kegiatanId = kegiatanSnapshot.key

                    // Ambil nama kegiatan dari kegiatan_umum
                    val kegiatanUmumRef = FirebaseDatabase.getInstance().getReference("kegiatan_umum").child(kegiatanId!!)
                    kegiatanUmumRef.child("nama").addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(namaSnapshot: DataSnapshot) {
                            val namaKegiatan = namaSnapshot.getValue(String::class.java)
                            Log.d("Firebase", "Nama kegiatan: $namaKegiatan")
                            // Lakukan sesuatu dengan namaKegiatan
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("Firebase", "Gagal ambil nama kegiatan: ${error.message}")
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Gagal ambil kegiatan_user: ${error.message}")
            }
        })
    }



    private fun saveAbsenToFirestore(imageUrl: String, lokasi: String) {
        val kegiatanId = intent.getStringExtra("id_kegiatan")
        if (kegiatanId.isNullOrEmpty()) {
            Toast.makeText(this, "ID kegiatan tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        // Ambil nama kegiatan dulu dari Realtime Database
        val kegiatanRef = FirebaseDatabase.getInstance().getReference("kegiatan_umum").child(kegiatanId)
        kegiatanRef.child("nama").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val namaKegiatan = snapshot.getValue(String::class.java) ?: "Tidak diketahui"

                // Siapkan data absen setelah nama kegiatan berhasil diambil
                val absenData = mapOf<String, Any>(
                    "uid" to (user?.uid ?: ""),
                    "kegiatan" to namaKegiatan,
                    "status" to (selectedStatus ?: ""),
                    "lokasi" to lokasi,
                    "imageUrl" to imageUrl,
                    "timestamp" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                )

                firestore.collection("absensi")
                    .add(absenData)
                    .addOnSuccessListener {
                        Toast.makeText(this@AbsenActivity, "Absen berhasil!", Toast.LENGTH_SHORT).show()

                        // Simpan juga ke Realtime DB
                        saveAbsenToRealtimeDatabase(absenData)

                        setResult(RESULT_OK)
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this@AbsenActivity, "Gagal menyimpan data", Toast.LENGTH_SHORT).show()
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AbsenActivity, "Gagal mengambil nama kegiatan", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun saveAbsenToRealtimeDatabase(absenData: Map<String, Any>) {
        val kegiatanId = intent.getStringExtra("id_kegiatan") ?: return
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val databaseRef = FirebaseDatabase.getInstance()
            .getReference("absensi")
            .child(kegiatanId)
            .child(uid)

        // Hapus key 'uid' dan 'kegiatan' dari data yang akan disimpan (sudah tergambar dari struktur)
        val cleanedData = absenData.filterKeys { it != "uid" && it != "kegiatan" }

        databaseRef.setValue(cleanedData)
            .addOnSuccessListener {
                Toast.makeText(this, "Juga disimpan ke Realtime Database!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal simpan ke Realtime DB", Toast.LENGTH_SHORT).show()
            }
    }

}