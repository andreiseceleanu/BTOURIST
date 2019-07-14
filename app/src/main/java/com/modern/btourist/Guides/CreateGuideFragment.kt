package com.modern.btourist.Guides


import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.navigation.findNavController
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.modern.btourist.Database.Attraction

import com.modern.btourist.R
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.fragment_create_guide.*
import java.io.Serializable

class CreateGuideFragment : Fragment() {

    lateinit var selectedAttractionsList: ArrayList<String>
    lateinit var attractionList: ArrayList<Attraction>
    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var col: CollectionReference = db.collection("attractions")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_create_guide, container, false)

        if(savedInstanceState!=null){
            selectedAttractionsList.addAll(savedInstanceState.getStringArrayList("list"))
        }else{
            selectedAttractionsList = ArrayList()
        }


        var bundle = CreateGuideFragmentArgs.fromBundle(arguments!!)
        var item = bundle.selected

        var sharedPrefs: SharedPreferences = activity!!.getSharedPreferences("com.modern.btourist.prefs",0)
        val editor = sharedPrefs.edit()
        var storedList = sharedPrefs.getStringSet("AttractionList",null)
        if(storedList!=null){
            selectedAttractionsList.addAll(storedList.toTypedArray())
        }
        selectedAttractionsList.add(item)
        editor.remove("AttractionList")
        editor.putStringSet("AttractionList",selectedAttractionsList.toSet())
        editor.apply()

        lateinit var category: String

        val cultureButton = view.findViewById<Button>(R.id.cultureButton)
        val natureButton = view.findViewById<Button>(R.id.natureButton)
        val shoppingButton = view.findViewById<Button>(R.id.shoppingButton)
        val foodButton = view.findViewById<Button>(R.id.foodButton)
        val nightlifeButton = view.findViewById<Button>(R.id.nightlifeButton)

        cultureButton.setOnClickListener {
            category = "Culture, Architecture and History"
            view.findNavController().navigate(CreateGuideFragmentDirections.actionCreateGuideFragmentToCreateGuideAttractions(category))

        }

        natureButton.setOnClickListener {
            category = "Nature"
            view.findNavController().navigate(CreateGuideFragmentDirections.actionCreateGuideFragmentToCreateGuideAttractions(category))

        }

        shoppingButton.setOnClickListener {
            category = "Shopping"
            view.findNavController().navigate(CreateGuideFragmentDirections.actionCreateGuideFragmentToCreateGuideAttractions(category))
        }

        foodButton.setOnClickListener {
            category = "Food"
            view.findNavController().navigate(CreateGuideFragmentDirections.actionCreateGuideFragmentToCreateGuideAttractions(category))
        }

        nightlifeButton.setOnClickListener {
            category = "Nightlife"
            view.findNavController().navigate(CreateGuideFragmentDirections.actionCreateGuideFragmentToCreateGuideAttractions(category))
        }

        col.addSnapshotListener(activity as Activity) { querySnapshot, firebaseFirestoreException ->
            attractionList = ArrayList()
            if (firebaseFirestoreException != null) {
                Log.e("MapFragment", "Attraction snapshot listen failed", firebaseFirestoreException)
            }
            if (querySnapshot != null) {
                for (document in querySnapshot) {
                    var attraction: Attraction = document.toObject(Attraction::class.java)
                    for( a in selectedAttractionsList)
                    {
                     if(a == attraction.name){
                         attractionList.add(attraction)
                     }
                    }
                }
                val listView = view.findViewById<ListView>(R.id.createShowList)
                var adapter = ShowListAdapter(view.context, attractionList)
                listView.adapter = adapter

                val nextButton = view.findViewById<Button>(R.id.nextCreateButton)
                val clearButton = view.findViewById<Button>(R.id.clearButton)

                clearButton.setOnClickListener {
                    attractionList.clear()
                    adapter.notifyDataSetChanged()
                    editor.remove("AttractionList")
                    editor.apply()
                }

                nextButton.setOnClickListener {
                    view.findNavController().navigate(CreateGuideFragmentDirections.actionCreateGuideFragmentToCreateGuide2Fragment(attractionList.toTypedArray()))
                }

            }
        }

        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList("list",selectedAttractionsList)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if(savedInstanceState!=null)
        selectedAttractionsList.addAll(savedInstanceState.getStringArrayList("list"))
    }
}
