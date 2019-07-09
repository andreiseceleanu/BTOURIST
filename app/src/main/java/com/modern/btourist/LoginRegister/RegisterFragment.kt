package com.modern.btourist.LoginRegister


import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.modern.btourist.databinding.FragmentRegisterBinding
import kotlinx.android.synthetic.main.fragment_register.*





class RegisterFragment : Fragment() {

    private var validateFirstName: Boolean = true
    private var validateLastName: Boolean = true
    private var validateEmail: Boolean = true
    private var validatePass: Boolean = true
    private var validatePhone: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = DataBindingUtil.inflate<FragmentRegisterBinding>(inflater,
            com.modern.btourist.R.layout.fragment_register,container,false)

        val nextButton = binding.registerNextButton1


        nextButton.setOnClickListener({registerUser()})

        return binding.root
    }

    private fun validateFirstName(): Boolean{
        var firstName: String = firstNameText.getText().toString().trim()

        if (firstName.isEmpty()) {
            firstNameText.requestFocus()
            firstNameText.error = "Enter First Name"
            return false
        }else {
            firstNameText.error = null
            return true
        }

    }

    private fun validateLastName(): Boolean{
        var lastName: String = lastNameText.getText().toString().trim()

        if (lastName.isEmpty()) {
            lastNameText.requestFocus()
            lastNameText.error = "Enter Last Name"
            return false
        }else {
            lastNameText.error = null
            return true
        }
    }

    private fun validateEmail(): Boolean{
        var email: String = emailText.getText().toString().trim()

        if (email.isEmpty()) {
            emailText.requestFocus()
            emailText.error = "Email is required"
            return false
        } else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailText.requestFocus()
                emailText.error = "Please enter a valid email"
                return false
        }else {
            emailText.error = null
            return true
        }
    }

    private fun validatePassword(): Boolean{
        var password: String = passRegText.getText().toString().trim()

        if (password.isEmpty()) {
            passRegText.requestFocus()
            passRegText.error="Password cannot be empty!"
            return false
        }else if (password.length < 8)
        {
            passRegText.requestFocus()
            passRegText.setError("Minimum lenght of the password is 8")
            return false
        }else {
            passRegText.error = null
            return true
        }
    }

    fun isNumeric(s: String?): Boolean {
        return s != null && s.matches("[-+]?\\d*\\.?\\d+".toRegex())
    }

    private fun validatePhone(): Boolean{
        var phone: String = phoneText.getText().toString().trim()

        if (phone.isEmpty()) {
            phoneText.requestFocus()
            phoneText.error = "Enter Phone Number"
            return false
        }else if(!isNumeric(phone)){
            phoneText.requestFocus()
            phoneText.error = "Phone must be numeric"
            return false
        }else {
            phoneText.error = null
            return true
        }
    }

    private fun registerUser(){

        var firstName: String = firstNameText.getText().toString().trim()
        var lastName: String = lastNameText.getText().toString().trim()
        var email: String = emailText.getText().toString().trim()
        var password: String = passRegText.getText().toString().trim()
        var phone: String = phoneText.getText().toString().trim()


        if(validatePhone() && validatePassword() && validateFirstName() && validateLastName() && validateEmail()) {

            var phoneInt: Long = phone.toLong()


            view!!.findNavController().navigate(RegisterFragmentDirections.actionRegisterFragmentToInterestsFragment(firstName,lastName,email,phoneInt,password))

        }

    }


}
