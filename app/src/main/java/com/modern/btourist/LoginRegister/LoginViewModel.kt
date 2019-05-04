package com.modern.btourist.LoginRegister

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth



class LoginViewModel: ViewModel(){
    private lateinit var mAuth: FirebaseAuth

    init{
        Log.i("LoginViewModel","LoginViewModel Created")
        mAuth = FirebaseAuth.getInstance()
    }


    override fun onCleared() {
        super.onCleared()
        Log.i("LoginViewModel","LoginViewModel Destroyed")
    }
}