package com.example.eventra1.view.login

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.eventra1.view.profile.ProfileActivity
import com.example.eventra1.R
import com.example.eventra1.utils.SessionLogin
import com.example.eventra1.view.main.MainActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {

    lateinit var session: SessionLogin
    lateinit var strNama: String
    lateinit var strPassword: String
    var REQ_PERMISSION = 101

    // Declare variables
    lateinit var btnLogin: MaterialButton
    lateinit var inputNama: EditText
    lateinit var inputPassword: EditText
    lateinit var mAuth: FirebaseAuth



    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // INISIALISASI SESSION HARUS DULUAN
        session = SessionLogin(applicationContext)

        // Baru lakukan pengecekan
        if (session.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // Inisialisasi view lainnya
        inputNama = findViewById(R.id.inputNama)
        inputPassword = findViewById(R.id.inputPassword)
        btnLogin = findViewById(R.id.btnLogin)
        mAuth = FirebaseAuth.getInstance()

        setPermission()
        setInitLayout()
    }


    private fun setPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQ_PERMISSION
            )
        }
    }

    private fun setInitLayout() {
        session = SessionLogin(applicationContext)

        if (session.isLoggedIn()) {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }

        btnLogin.setOnClickListener {
            val email = inputNama.text.toString()
            val password = inputPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Form tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            } else {
                login(email, password) // Firebase Login
            }
        }


    }

    fun signUp(email: String?, password: String?) {
        if (!validateForm()) return

        mAuth.createUserWithEmailAndPassword(email!!, password!!)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    Toast.makeText(
                        this,
                        "Pendaftaran berhasil, selamat datang: ${user?.email}",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateUIDaftar(user)
                } else {
                    Toast.makeText(
                        this,
                        "Gagal mendaftar: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateUIDaftar(null)
                }
            }
    }

    fun login(email: String?, password: String?) {
        if (!validateForm()) return

        mAuth.signInWithEmailAndPassword(email!!, password!!)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    Toast.makeText(this, "Login berhasil: ${user?.email}", Toast.LENGTH_SHORT)
                        .show()
                    updateUILogin(user)
                } else {
                    Toast.makeText(
                        this,
                        "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateUILogin(null)
                }
            }


        fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
        ) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            for (grantResult in grantResults) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    val intent = intent
                    finish()
                    startActivity(intent)
                }
            }
        }

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
    fun validateForm(): Boolean {
        var result = true
        if (TextUtils.isEmpty(inputNama.text.toString())) {
            inputNama.error = "Required"
            result = false
        } else {
            inputNama.error = null
        }

        if (TextUtils.isEmpty(inputPassword.text.toString())) {
            inputPassword.error = "Required"
            result = false
        } else {
            inputPassword.error = null
        }
        return result
    }

    fun updateUIDaftar(user: FirebaseUser?) {
        if (user != null) {
            // Jika berhasil login, arahkan ke activity berikutnya
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // Jika tidak login, tetap di activity login
            Toast.makeText(this, "Sign Up In First", Toast.LENGTH_SHORT).show()
        }
    }
    fun updateUILogin(user: FirebaseUser?) {
        Log.d("Login", "User: $user")
        if (user != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Log In First", Toast.LENGTH_SHORT).show()
        }
    }
}
