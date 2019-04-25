package com.modern.btourist


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.modern.btourist.databinding.FragmentRegisterBinding


class RegisterFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = DataBindingUtil.inflate<FragmentRegisterBinding>(inflater,R.layout.fragment_register,container,false)
        val nextButton = binding.registerNextButton1

        nextButton.setOnClickListener({view: View->view.findNavController().navigate(RegisterFragmentDirections.actionRegisterFragmentToInterestsFragment())})

        return binding.root
    }


}
