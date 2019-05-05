package com.modern.btourist.LoginRegister


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.modern.btourist.R
import com.modern.btourist.databinding.FragmentInterestsBinding

class InterestsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentInterestsBinding>(inflater, R.layout.fragment_interests,container,false)
        val interests: ArrayList<String> = ArrayList()
        val iterator: Iterator<String> = interests.iterator()
        interests.add("Explore the city")
        interests.add("Enjoy nature")
        interests.add("Shopping")
        interests.add("Eating out")
        interests.add("Go partying")
        interests.add("Culture, architecture and history")

            var spinner1: Spinner = binding.spinner
            var spinner2: Spinner = binding.spinner2
            var spinner3: Spinner = binding.spinner3

            var adapter: ArrayAdapter<String> = ArrayAdapter(context,android.R.layout.simple_spinner_item,interests)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner1.setAdapter(adapter)
        var interest1: String = spinner1.selectedItem.toString().trim()

        spinner2.setAdapter(adapter)
        var interest2: String = spinner2.selectedItem.toString().trim()

        spinner3.setAdapter(adapter)
        var interest3: String = spinner1.selectedItem.toString().trim()

        var languageText1: String = binding.firstLanguageText.getText().toString().trim()
        var languageText2: String = binding.secondLanguageText.getText().toString().trim()

        var args = InterestsFragmentArgs.fromBundle(arguments!!)

        var nextButton = binding.toProfilePictureButton
        nextButton.setOnClickListener{
            view!!.findNavController().navigate(InterestsFragmentDirections.actionInterestsFragmentToProfilePictureFragment(args.email,args.firstName,args.lastName,args.phone,interest1,
                interest2,interest3,languageText1,languageText2))
        }



        return binding.root
    }


}
