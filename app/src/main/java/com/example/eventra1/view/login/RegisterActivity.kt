package com.example.eventra1.view.login

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.eventra1.ProfileActivity
import com.example.eventra1.R
import com.example.eventra1.utils.SessionLogin
import com.example.eventra1.view.main.MainActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class RegisterActivity : AppCompatActivity() {

    lateinit var session: SessionLogin
    lateinit var strNama: String
    lateinit var strPassword: String
    var REQ_PERMISSION = 101

    // Declare variables
    lateinit var btnDaftar: MaterialButton
    lateinit var inputNama: EditText
    lateinit var inputPassword: EditText
    lateinit var inputConfirmPass: EditText
    lateinit var mAuth: FirebaseAuth



    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)


        // Inisialisasi view lainnya
        inputNama = findViewById(R.id.inputNama)
        inputPassword = findViewById(R.id.inputPassword)
        inputConfirmPass = findViewById(R.id.confirmPassword)
        btnDaftar = findViewById(R.id.btnDaftar)
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
            startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
            finish()
        }

        btnDaftar.setOnClickListener {
            signUp(inputNama.text.toString(), inputPassword.text.toString(), inputConfirmPass.text.toString())
        }

    }

    fun signUp(email: String?, password: String?, confirmPass: String?) {
        if (!validateForm(password!!, confirmPass!!)) return

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

    fun validateForm(password: String, confirmPass: String): Boolean {
        var result = true

        if (TextUtils.isEmpty(inputNama.text.toString())) {
            inputNama.error = "Required"
            result = false
        } else {
            inputNama.error = null
        }

        if (TextUtils.isEmpty(password)) {
            inputPassword.error = "Required"
            result = false
        } else {
            inputPassword.error = null
        }

        if (TextUtils.isEmpty(confirmPass)) {
            inputConfirmPass.error = "Required"
            result = false
        } else if (password != confirmPass) {
            inputConfirmPass.error = "Password tidak cocok"
            result = false
        } else {
            inputConfirmPass.error = null
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

}
