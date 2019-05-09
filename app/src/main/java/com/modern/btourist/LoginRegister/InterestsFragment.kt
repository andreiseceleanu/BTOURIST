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
import kotlinx.android.synthetic.main.fragment_interests.*

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
        spinner2.setAdapter(adapter)
        spinner3.setAdapter(adapter)


        var languageText1 = binding.firstLanguageText
        var languageText2 = binding.secondLanguageText


        var args = InterestsFragmentArgs.fromBundle(arguments!!)


        var nextButton = binding.toProfilePictureButton
        nextButton.setOnClickListener{
            if(validateLanguage1()&&validateLanguage2()){
                var interest1: String = spinner1.selectedItem.toString().trim()
                var interest2: String = spinner2.selectedItem.toString().trim()
                var interest3: String = spinner3.selectedItem.toString().trim()
                var languageString1 = languageText1.text.toString().trim()
                var languageString2 = languageText2.text.toString().trim()
            view!!.findNavController().navigate(InterestsFragmentDirections.actionInterestsFragmentToProfilePictureFragment(args.email,args.firstName,args.lastName,args.phone,interest1,
                interest2,interest3,languageString1,languageString2,args.password))
            }
        }



        return binding.root
    }

    fun validateLanguage1(): Boolean{
        var languageString1: String = firstLanguageText.text.toString().trim()

        if (languageString1.isEmpty()) {
            firstLanguageText.requestFocus()
            firstLanguageText.error = "Enter your first language"
            return false
        }else {
            firstLanguageText.error = null
            return true
        }
    }

    fun validateLanguage2(): Boolean{
        var languageString2: String = secondLanguageText.text.toString().trim()

        if (languageString2.isEmpty()) {
            secondLanguageText.requestFocus()
            secondLanguageText.error = "Enter your second language"
            return false
        }else {
            secondLanguageText.error = null
            return true
        }
    }

}
