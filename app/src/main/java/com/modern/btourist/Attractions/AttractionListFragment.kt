package com.modern.btourist.Attractions


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.modern.btourist.Database.Attraction

import com.modern.btourist.R
import com.modern.btourist.databinding.FragmentCategories2Binding

class AttractionListFragment : Fragment(){

    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var col: CollectionReference = db.collection("attractions")

    private lateinit var adapter: AttractionAddapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val user = inflater.inflate(R.layout.fragment_attraction_list, container, false)

        var bundle = AttractionListFragmentArgs.fromBundle(arguments!!)
        var category = bundle.category
        var query: Query = col.whereEqualTo("category", category)

        var options: FirestoreRecyclerOptions<Attraction> = FirestoreRecyclerOptions.Builder<Attraction>()
            .setQuery(query,Attraction::class.java)
            .build()

        adapter = AttractionAddapter(options)
        var recycleView: RecyclerView = user.findViewById(R.id.attractionRecyclerView)
        recycleView.setHasFixedSize(true)
        recycleView.layoutManager = LinearLayoutManager(context)
        recycleView.adapter = adapter

        adapter.setOnItemClickListener(object:AttractionAddapter.OnItemClickListener {
            override fun onItemClick(documentSnapshot: DocumentSnapshot, position: Int) {
                var attraction = documentSnapshot.toObject(Attraction::class.java)
                var name = attraction!!.name
                var description = attraction.description
                var phone = attraction.phone
                var website = attraction.website
                var image = attraction.image
                var latitude = attraction.latitude
                var longitude = attraction.longitude

                user.findNavController().navigate(AttractionListFragmentDirections.actionAttractionListFragmentToAttractionInfoFragment(name,description,phone,website,image))

            }

        })

        return user
    }



    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onPause() {
        super.onPause()
        adapter.stopListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    override fun onResume() {
        super.onResume()
        adapter.startListening()
    }


}
