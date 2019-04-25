package com.modern.btourist


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.modern.btourist.databinding.FragmentMapBinding

class MapFragment : Fragment() {

    lateinit var binding: FragmentMapBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_map,container,false)

        var emailTextView: TextView = binding.userText
        var user = FirebaseAuth.getInstance().currentUser

        emailTextView.setText(user!!.email)

        return binding.root

    }



}
