package com.modern.btourist.LoginRegister


import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.modern.btourist.R
import com.modern.btourist.LoginRegister.RegisterFragmentDirections
import com.modern.btourist.databinding.FragmentRegisterBinding
import kotlinx.android.synthetic.main.fragment_register.*


class RegisterFragment : Fragment() {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = DataBindingUtil.inflate<FragmentRegisterBinding>(inflater,
            R.layout.fragment_register,container,false)

        val nextButton = binding.registerNextButton1
        mAuth = FirebaseAuth.getInstance()

        nextButton.setOnClickListener({registerUser()})

        return binding.root
    }

    private fun registerUser(){

        var email: String = emailText.getText().toString().trim()
        var password: String = passRegText.getText().toString().trim()

        if (email.isEmpty()) {
            emailText.error = "Email is required"
            emailText.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.error = "Please enter a valid email"
            emailText.requestFocus()
            return
        }

        if (password.isEmpty()) {
            passRegText.error="Password cannot be empty!"
            passRegText.requestFocus()
            return
        }

        if (password.length < 8) {
            passRegText.setError("Minimum lenght of the password is 8")
            passRegText.requestFocus()
            return
        }

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task: Task<AuthResult> ->
            if (task.isSuccessful) {
                //Registration OK
                val firebaseUser = mAuth.currentUser!!
                view!!.findNavController().navigate(RegisterFragmentDirections.actionRegisterFragmentToLoginFragment())
            } else {
                //Registration error
                Snackbar.make(view!!,"Registration Failed", Snackbar.LENGTH_LONG).show()
            }
        }

    }


}
