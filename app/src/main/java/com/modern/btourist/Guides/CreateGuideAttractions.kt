package com.modern.btourist.Guides


import android.annotation.SuppressLint
import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.findNavController
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.modern.btourist.Database.Attraction

import com.modern.btourist.R

class CreateGuideAttractions : Fragment() {

    lateinit var selectedItems:ArrayList<String>
    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var col: CollectionReference = db.collection("attractions")

    @SuppressLint("CommitPrefEdits")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_create_guide_attractions, container, false)
        selectedItems = ArrayList()

        val chl = view.findViewById(R.id.createAttractionsList) as ListView

        col.addSnapshotListener(activity as Activity) { querySnapshot, firebaseFirestoreException ->
            var attractionList = ArrayList<String>()
            if (firebaseFirestoreException != null) {
                Log.e("MapFragment", "Attraction snapshot listen failed", firebaseFirestoreException)
            }
            if (querySnapshot != null) {
                for (document in querySnapshot) {
                    var bundle = CreateGuideAttractionsArgs.fromBundle(arguments!!)
                    var attraction: Attraction = document.toObject(Attraction::class.java)
                    if(attraction.category == bundle.category)
                    attractionList.add(attraction.name)
                }

                //supply data itmes to ListView
                val aa = ArrayAdapter(view.context, android.R.layout.simple_list_item_1, attractionList)
                chl.adapter = aa
                //set OnItemClickListener
                chl.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                    // selected item
                    val selectedItem = (view as TextView).getText().toString()
                    view.findNavController().navigate(CreateGuideAttractionsDirections.actionCreateGuideAttractionsToCreateGuideFragment(selectedItem))

                }
            }
        }

        return view
    }


}

