package com.example.eventra1.view.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.eventra1.view.profile.ProfileActivity.Companion.CLOUDINARY_UPLOAD_PRESET
import com.example.eventra1.view.profile.ProfileActivity.Companion.CLOUDINARY_UPLOAD_URL
import com.example.eventra1.R
import com.example.eventra1.utils.SessionLogin
import com.example.eventra1.view.login.LoginActivity
import com.example.eventra1.view.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class ProfileActivity : AppCompatActivity() {

    private lateinit var etNama: EditText
    private lateinit var etProgramStudi: EditText
    private lateinit var etFakultas: EditText
    private lateinit var etUniversitas: EditText
    private lateinit var imgProfile: ImageView
    private lateinit var btnSave: Button
    private lateinit var btnLogout: Button
    private var imageUri: Uri? = null
    private val firestore = FirebaseFirestore.getInstance()
    lateinit var session: SessionLogin


    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val namaRef = FirebaseDatabase.getInstance().getReference("profile").child(uid!!).child("nama")
    private val dbRef = FirebaseDatabase.getInstance().getReference("users")
    private lateinit var textViewNamaDisplay: TextView
    private val storageRef = FirebaseStorage.getInstance().reference.child("profile_pics")


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            val intent = Intent(this@ProfileActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val databaseRef = FirebaseDatabase.getInstance().getReference("profile").child(uid)
            databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nama = snapshot.child("nama").getValue(String::class.java)
                    val prodi = snapshot.child("prodi").getValue(String::class.java)
                    val fakultas = snapshot.child("fakultas").getValue(String::class.java)
                    val universitas = snapshot.child("universitas").getValue(String::class.java)

                    // Tampilkan data
                    if (nama != null) textViewNamaDisplay.text = nama
                    if (prodi != null) etProgramStudi.setText(prodi)
                    if (fakultas != null) etFakultas.setText(fakultas)
                    if (universitas != null) etUniversitas.setText(universitas)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ProfileActivity, "Gagal ambil data", Toast.LENGTH_SHORT)
                        .show()
                }
            })
        }

        session = SessionLogin(this) // 'this' = activity context
        session = SessionLogin(applicationContext)


        etNama = findViewById(R.id.etNama)
        textViewNamaDisplay = findViewById(R.id.textViewNamaDisplay)
        etProgramStudi = findViewById(R.id.etProgramStudi)
        etFakultas = findViewById(R.id.etFakultas)
        etUniversitas = findViewById(R.id.etUniversitas)
        imgProfile = findViewById(R.id.imgProfile)
        btnSave = findViewById(R.id.btnSave)
        btnLogout = findViewById(R.id.btnLogout)




        imgProfile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 101)
        }

        loadUserData()

        btnSave.setOnClickListener {
            val nama = etNama.text.toString()
            val programStudi = etProgramStudi.text.toString()
            val fakultas = etFakultas.text.toString()
            val universitas = etUniversitas.text.toString()

            if (nama.isEmpty() || programStudi.isEmpty() || fakultas.isEmpty() || universitas.isEmpty()) {
                Toast.makeText(this, "Semua kolom harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid == null) {
                Toast.makeText(this, "User belum login", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val profileData = hashMapOf<String, Any>(
                "nama" to nama,
                "programStudi" to programStudi,
                "fakultas" to fakultas,
                "universitas" to universitas
            )

            if (imageUri != null) {
                uploadImageToCloudinary(imageUri!!) { imageUrl ->
                    if (imageUrl != null) {
                        profileData["profileUrl"] = imageUrl
                    }

                    dbRef.child(uid).setValue(profileData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Profil berhasil disimpan", Toast.LENGTH_SHORT)
                                .show()
                            loadUserData()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Gagal simpan profil", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                dbRef.child(uid).setValue(profileData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Profil berhasil disimpan", Toast.LENGTH_SHORT).show()
                        loadUserData()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal simpan profil", Toast.LENGTH_SHORT).show()
                    }
            }

            // Simpan ke Firestore (kalau tetap dipakai)
            firestore.collection("profile")
                .add(profileData)
                .addOnSuccessListener {
                    // Lanjut simpan ke Realtime Database pakai UID
                    val databaseRef =
                        FirebaseDatabase.getInstance().getReference("profile").child(uid)
                    databaseRef.setValue(profileData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Profil berhasil disimpan!", Toast.LENGTH_SHORT)
                                .show()

                            // ✅ Update tampilan nama
                            textViewNamaDisplay.text = nama
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Gagal simpan ke Realtime DB", Toast.LENGTH_SHORT)
                                .show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal menyimpan ke Firestore", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun uploadImageToCloudinary(imageUri: Uri, callback: (String?) -> Unit) {
        val inputStream = contentResolver.openInputStream(imageUri)
        val bytes = inputStream?.readBytes() ?: run {
            callback(null)
            return
        }
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", "profile.jpg", bytes.toRequestBody())
            .addFormDataPart("upload_preset", CLOUDINARY_UPLOAD_PRESET)
            .build()

        val request = Request.Builder()
            .url(CLOUDINARY_UPLOAD_URL)
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@ProfileActivity, "Upload gambar gagal", Toast.LENGTH_SHORT)
                        .show()
                    callback(null)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                val json = responseData?.let { JSONObject(it) }
                val imageUrl = json?.optString("secure_url", null)
                runOnUiThread {
                    callback(imageUrl)
                }
            }
        })


        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            session.logoutUser()
        }


        /*btnSave.setOnClickListener {
            saveUserData()
        }*/
    }

    private fun saveProfileToFirestrore() {
        val profileData = mapOf<String, Any>(
            "nama" to etNama.text.toString(),
            "prodi" to etProgramStudi.text.toString(),
            "fakultas" to etFakultas.text.toString(),
            "universitas" to etUniversitas.text.toString(),
            // Tambahkan image URL jika sudah ada
        )

        firestore.collection("profile")
            .add(profileData)
            .addOnSuccessListener {
                Toast.makeText(this, "Profil berhasil disimpan!", Toast.LENGTH_SHORT).show()

                // Simpan juga ke Realtime Database
                saveAbsenToRealtimeDatabase(profileData)

                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menyimpan data", Toast.LENGTH_SHORT).show()
            }
    }


    private fun saveAbsenToRealtimeDatabase(profileData: Map<String, Any>) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "UID tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        val databaseRef = FirebaseDatabase.getInstance().getReference("profile").child(uid)
        databaseRef.setValue(profileData)
            .addOnSuccessListener {
                Toast.makeText(this, "Data disimpan ke Realtime Database!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal simpan ke Realtime DB", Toast.LENGTH_SHORT).show()
            }
    }


    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java) // Ganti LoginActivity dengan activity login kamu
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }


    private fun loadUserData() {
        userId?.let {
            dbRef.child(it).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    etNama.setText(snapshot.child("nama").value?.toString())
                    etProgramStudi.setText(snapshot.child("programStudi").value?.toString())
                    etFakultas.setText(snapshot.child("fakultas").value?.toString())
                    etUniversitas.setText(snapshot.child("universitas").value?.toString())
                    val profileUrl = snapshot.child("profileUrl").value?.toString()

                    if (!profileUrl.isNullOrEmpty()) {
                        Glide.with(this@ProfileActivity).load(profileUrl).into(imgProfile)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ProfileActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            imageUri = data.data
            imgProfile.setImageURI(imageUri)
        }
    }

    companion object {
        const val CLOUDINARY_UPLOAD_URL = "https://api.cloudinary.com/v1_1/dy34e4xsd/image/upload"
        const val CLOUDINARY_UPLOAD_PRESET = "eventra"
    }


}