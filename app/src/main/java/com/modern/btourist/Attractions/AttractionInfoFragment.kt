package com.modern.btourist.Attractions


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.modern.btourist.Database.Btourist

import com.modern.btourist.R
import com.modern.btourist.databinding.FragmentAttractionInfoBinding
import com.modern.btourist.databinding.FragmentCategories2Binding

class AttractionInfoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val binding = DataBindingUtil.inflate<FragmentAttractionInfoBinding>(inflater,
            R.layout.fragment_attraction_info, container, false)


        val imageView = binding.attractionImage
        val nameText = binding.nameText
        val descriptionText = binding.descriptionText
        val phoneText = binding.phoneText
        val websiteText = binding.websiteText

        var bundle = AttractionInfoFragmentArgs.fromBundle(arguments!!)
        var resource = bundle.image
        var img = getImage(resource)
        var name = bundle.name
        var description = bundle.description
        var phone = bundle.phone
        var website = bundle.website

        imageView.setImageResource(img)
        nameText.text = name
        descriptionText.text = description
        phoneText.text = "Phone: "+ phone.toString()
        websiteText.text = "Website: "+ website

        var button = binding.button
        button.setOnClickListener {
            it.findNavController().navigate(AttractionInfoFragmentDirections.actionAttractionInfoFragmentToMapFragment(name))
        }

        return binding.root
    }

    fun getImage(imageName: String): Int {

        var app = Btourist.instance.applicationContext

        var drawableResourceId = app.resources.getIdentifier(imageName, "drawable", app.packageName)

        return drawableResourceId
    }
}
