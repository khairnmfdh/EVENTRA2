package com.example.eventra1.utils

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.example.eventra1.view.login.LoginActivity

class SessionLogin(var context: Context) {
    var pref: SharedPreferences
    var editor: SharedPreferences.Editor
    var PRIVATE_MODE = 0

    fun createLoginSession(nama: String) {
        editor.putBoolean(IS_LOGIN, true)
        editor.putString(KEY_NAMA, nama)
        editor.commit()
    }

    fun checkLogin() {
        if (!isLoggedIn()) {
            val intent = Intent(context, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    fun logoutUser() {
        editor.clear()
        editor.commit()
        val intent = Intent(context, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    fun isLoggedIn(): Boolean = pref.getBoolean(IS_LOGIN, false)

    companion object {
        private const val PREF_NAME = "AbsensiPref"
        private const val IS_LOGIN = "IsLoggedIn"
        const val KEY_NAMA = "NAMA"
    }

    init {
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        editor = pref.edit()
    }
}