package com.modern.btourist.Info


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.modern.btourist.R


class InfoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_info, container, false)

        var bundle = InfoFragmentArgs.fromBundle(arguments!!)
        var list = bundle.infoList!!.toMutableList()
        var poisition = bundle.position

        var infoTextView = view.findViewById<TextView>(R.id.infoTextView)
        infoTextView.text = list[poisition]

        return view
    }


}
