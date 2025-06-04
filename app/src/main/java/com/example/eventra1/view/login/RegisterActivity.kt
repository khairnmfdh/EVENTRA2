package com.example.eventra1.view.login

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
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

class RegisterActivity : AppCompatActivity() {

    lateinit var session: SessionLogin
    lateinit var strEmail: String
    lateinit var strPassword: String
    var REQ_PERMISSION = 101

    // Declare variables
    lateinit var btnDaftar: MaterialButton
    lateinit var inputEmail: EditText
    lateinit var inputPassword: EditText
    lateinit var inputConfirmPass: EditText
    lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        inputEmail = findViewById(R.id.inputEmail)
        inputPassword = findViewById(R.id.inputPassword)
        inputConfirmPass = findViewById(R.id.confirmPassword)
        btnDaftar = findViewById(R.id.btnDaftar)
        mAuth = FirebaseAuth.getInstance()

        // Optional: Permission jika memang akan digunakan
        // setPermission()

        setInitLayout()
    }

    private fun setPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
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
            signUp(inputEmail.text.toString(), inputPassword.text.toString(), inputConfirmPass.text.toString())
        }
    }

    fun signUp(email: String?, password: String?, confirmPass: String?) {
        if (!validateForm(email!!, password!!, confirmPass!!)) return

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    Toast.makeText(
                        this,
                        "Pendaftaran berhasil, selamat datang: ${user?.email}",
                        Toast.LENGTH_SHORT
                    ).show()
                    goToProfileActivity(user)
                } else {
                    Toast.makeText(
                        this,
                        "Gagal mendaftar: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    goToProfileActivity(null)
                }
            }
    }

    fun validateForm(email: String, password: String, confirmPass: String): Boolean {
        var result = true

        if (TextUtils.isEmpty(email)) {
            inputEmail.error = "Email tidak boleh kosong"
            result = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            inputEmail.error = "Format email tidak valid"
            result = false
        } else {
            inputEmail.error = null
        }

        if (TextUtils.isEmpty(password)) {
            inputPassword.error = "Password tidak boleh kosong"
            result = false
        } else {
            inputPassword.error = null
        }

        if (TextUtils.isEmpty(confirmPass)) {
            inputConfirmPass.error = "Konfirmasi password tidak boleh kosong"
            result = false
        } else if (password != confirmPass) {
            inputConfirmPass.error = "Password tidak cocok"
            result = false
        } else {
            inputConfirmPass.error = null
        }

        return result
    }

    fun goToProfileActivity(user: FirebaseUser?) {
        if (user != null) {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Sign Up In First", Toast.LENGTH_SHORT).show()
        }
    }
}
