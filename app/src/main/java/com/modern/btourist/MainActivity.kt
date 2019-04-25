package com.modern.btourist

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.modern.btourist.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    //private lateinit var viewModel: LoginViewModel
    //private lateinit var mAuth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)

    }

    //Create overflow menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    //Log out selected -> Set AUTHENTIFICATED=false in Shared Preferences
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(item!!.itemId==R.id.logOutItem){
            val PREFS_FILENAME = "com.modern.btourist.prefs"
            val prefs = this.getSharedPreferences(PREFS_FILENAME, 0)
            val editor = prefs.edit()
            editor.putBoolean("AUTHENTIFICATED",false)
            editor.apply()

            //Start login activity, Close main activity
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
            this.finish()

        }
        return super.onOptionsItemSelected(item)
    }



}
