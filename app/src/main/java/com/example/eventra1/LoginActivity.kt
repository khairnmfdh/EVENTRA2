package com.example.eventra1.view.login

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.eventra1.R
import com.example.eventra1.utils.SessionLogin
import com.example.eventra1.view.main.MainActivity
import com.google.android.material.button.MaterialButton

class LoginActivity : AppCompatActivity() {

    lateinit var session: SessionLogin
    lateinit var strNama: String
    lateinit var strPassword: String
    var REQ_PERMISSION = 101

    // Declare variables
    lateinit var btnLogin: MaterialButton
    lateinit var inputNama: EditText
    lateinit var inputPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize views after setContentView
        btnLogin = findViewById(R.id.btnLogin)
        inputNama = findViewById(R.id.inputNama)
        inputPassword = findViewById(R.id.inputPassword)

        setPermission()
        setInitLayout()
    }

    private fun setPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
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
            strNama = inputNama.text.toString()
            strPassword = inputPassword.text.toString()

            if (strNama.isEmpty() || strPassword.isEmpty()) {
                Toast.makeText(this@LoginActivity, "Form tidak boleh kosong!",
                    Toast.LENGTH_SHORT).show()
            } else {
                session.createLoginSession(strNama)
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            }
        }
    }

    override fun onRequestPermissionsResult(
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}
