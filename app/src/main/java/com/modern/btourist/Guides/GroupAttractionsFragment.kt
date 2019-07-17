package com.modern.btourist.Guides


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.modern.btourist.Database.Attraction
import com.modern.btourist.Database.User

import com.modern.btourist.R

class GroupAttractionsFragment : Fragment() {

    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var col: CollectionReference = db.collection("attractions")
    lateinit var registration: ListenerRegistration

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_group_attractions, container, false)

        val bundle = GroupAttractionsFragmentArgs.fromBundle(arguments!!)
        val recycleView = view.findViewById<ListView>(R.id.attractionsListView)

        registration = col.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if( firebaseFirestoreException!=null){
                Log.e("AttractionList","Snapshot listen failed",firebaseFirestoreException)
            }else{
                if(querySnapshot!=null) {
                    val list = bundle.attractionList!!
                    var attractionList: ArrayList<Attraction> = ArrayList()
                    for(u in list){

                        for(document in querySnapshot) {

                            var attraction = document.toObject(Attraction::class.java)
                            if (attraction.name == u){
                                attractionList.add(attraction)
                            }

                        }
                    }

                    var adapter = AttractionAdapter(this.context!!,R.layout.attraction_item_2,attractionList)
                    recycleView.adapter = adapter
                }
            }
        }

        return view
    }

    override fun onPause() {
        super.onPause()
        registration.remove()
    }

    override fun onStop() {
        super.onStop()
        registration.remove()
    }
}
