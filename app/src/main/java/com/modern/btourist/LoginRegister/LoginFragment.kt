package com.modern.btourist.LoginRegister


import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.modern.btourist.LoginRegister.LoginFragmentDirections
import com.modern.btourist.R
import com.modern.btourist.databinding.FragmentLoginBinding
import kotlinx.android.synthetic.main.fragment_login.*


class LoginFragment : Fragment() {

    private lateinit var viewModel: LoginViewModel
    private lateinit var mAuth: FirebaseAuth
    val PREFS_FILENAME = "com.modern.btourist.prefs"
    lateinit var prefs: SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        //Request Login View Model
        viewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)
        //DataBind layout to binding val
        val binding = DataBindingUtil.inflate<FragmentLoginBinding>(inflater,
            R.layout.fragment_login, container, false)
        val registerText = binding.registerText
        val loginButton = binding.loginButton

        //Navigate to register screen using Navigation Component
        registerText.setOnClickListener(View.OnClickListener { view!!.findNavController().navigate(
            LoginFragmentDirections.actionLoginFragmentToRegisterFragment()) })
        //Click on login button
        loginButton.setOnClickListener({userLogin() })

        return binding.root
    }

    fun userLogin(){
        mAuth = FirebaseAuth.getInstance()
        var email: String = loginNameText.getText().toString().trim()
        var password: String = passLoginText.getText().toString().trim()

        if (email.isEmpty()) {
            loginNameText.setError("Email is required")
            loginNameText.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            loginNameText.setError("Please enter a valid email")
            loginNameText.requestFocus()
            return
        }

        if (password.isEmpty()) {
            passLoginText.setError("Enter password")
            passLoginText.requestFocus()
            return
        }

        if (password.length < 8) {
            passLoginText.setError("Minimum lenght of password should be 8")
            passLoginText.requestFocus()
            return
        }

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task: Task<AuthResult> ->
            if (task.isSuccessful) {
                //Registration OK
                //Navigate to main activity
                view!!.findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToMainActivity2())

                //Set AUTHENTIFICATED=true in Shared Preferences
                val prefs = this.getActivity()!!.getSharedPreferences(PREFS_FILENAME, 0)
                val editor = prefs.edit()
                editor.putBoolean("AUTHENTIFICATED",true)
                editor.apply()

                //Stop Login Activity
                getActivity()!!.finish()

            } else {
                //Registration error using Snackbar
                Snackbar.make(view!!,"Wrong Credentials", Snackbar.LENGTH_LONG).show()
            }
        }


    }


}
