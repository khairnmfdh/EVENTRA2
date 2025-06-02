package com.example.eventra1.view.main

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.eventra1.ProfileActivity
import com.example.eventra1.R
import com.example.eventra1.utils.SessionLogin
import com.example.eventra1.view.absen.AbsenActivity
import com.example.eventra1.view.login.LoginActivity
import com.google.android.gms.cast.framework.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.example.eventra1.databinding.ActivityMainBinding
import com.example.eventra1.databinding.CardCyberBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    data class Kegiatan(val id: View, val nama: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val kegiatanList = listOf(
            Kegiatan(findViewById(R.id.cardCyber), "Workshop Cyber Security"),
        )


        kegiatanList.forEach { kegiatan ->
            kegiatan.id.setOnClickListener {
                bukaActivityAbsen(kegiatan.nama)
            }
        }

        binding.imageProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Listener setiap kartu
        binding.cardCyber.root.setOnClickListener {
            bukaActivityAbsen("Workshop Cyber Security")
        }

        binding.cardMobile.root.setOnClickListener {
            bukaActivityAbsen("Pengembangan Aplikasi Mobile")
        }

        binding.cardExcel.root.setOnClickListener {
            bukaActivityAbsen("Pelatihan Microsoft Excel")
        }
    }

    private fun bukaActivityAbsen(namaKegiatan: String) {
        val intent = Intent(this, AbsenActivity::class.java)
        intent.putExtra("NAMA_KEGIATAN", namaKegiatan)
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
