package com.modern.btourist

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    val PREFS_FILENAME = "com.modern.btourist.prefs"
    lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Get Shared Preferences, AUTHENTIFICATED=false for a fresh start of the app
        prefs = this.getSharedPreferences(PREFS_FILENAME, 0)
        val authentificated = prefs.getBoolean("AUTHENTIFICATED", false)
        //if false Inflate Login Activity layout
        if(authentificated==false){
            setContentView(R.layout.activity_login)
        }
        //else if true Start Main Activity
        else {
           val intent: Intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            this.finish()
        }

    }
}
