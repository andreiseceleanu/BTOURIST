package com.modern.btourist


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.modern.btourist.databinding.FragmentLoginBinding


class LoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val binding = DataBindingUtil.inflate<FragmentLoginBinding>(inflater, R.layout.fragment_login, container, false)

        val registerText = binding.registerText
        val loginButton = binding.loginButton

        registerText.setOnClickListener(View.OnClickListener { view!!.findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment()) })
        loginButton.setOnClickListener(View.OnClickListener { view!!.findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToMainActivity2()) })

        return binding.root

    }


}
