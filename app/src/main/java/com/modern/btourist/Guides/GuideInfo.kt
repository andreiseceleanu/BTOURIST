package com.modern.btourist.Guides


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.modern.btourist.R
import com.modern.btourist.databinding.FragmentGuideBinding


class GuideInfo : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = DataBindingUtil.inflate<FragmentGuideBinding>(inflater,
            R.layout.fragment_guide, container, false)

        val nameText = binding.guideNameText
        val descriptionText = binding.guideDescriptionText
        val userRecyclerView = binding.userRecycleView
        val attractionRecyclerView = binding.attractionsListView

        val bundle = GuideInfoArgs.fromBundle(arguments!!)
        nameText.text = bundle.name
        descriptionText.text = bundle.description


        return binding.root
    }


}
