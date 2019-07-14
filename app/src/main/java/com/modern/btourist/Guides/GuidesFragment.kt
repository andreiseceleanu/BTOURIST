package com.modern.btourist.Guides


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.modern.btourist.Attractions.AttractionAddapter
import com.modern.btourist.Attractions.AttractionListFragmentDirections
import com.modern.btourist.Database.Attraction
import com.modern.btourist.Database.Guide
import com.modern.btourist.R

class GuidesFragment : Fragment() {
    private lateinit var adapter: GuideAdapter
    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var col: CollectionReference = db.collection("guides")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val inflate = inflater.inflate(R.layout.fragment_list, container, false)
        val button: FloatingActionButton = inflate.findViewById(R.id.addGuideFloatingButton)

        button.setOnClickListener{
            it.findNavController().navigate(GuidesFragmentDirections.actionGuidesFragmentToCreateGuideFragment(""))
        }

        var query: Query = col.orderBy("owner",Query.Direction.DESCENDING)

        var options: FirestoreRecyclerOptions<Guide> = FirestoreRecyclerOptions.Builder<Guide>()
            .setQuery(query, Guide::class.java)
            .build()

        adapter = GuideAdapter(options)
        var recycleView: RecyclerView = inflate.findViewById(R.id.guidesRecyclerView)
        recycleView.setHasFixedSize(true)
        recycleView.layoutManager = LinearLayoutManager(context)
        recycleView.adapter = adapter

        adapter.setOnItemClickListener(object: GuideAdapter.OnItemClickListener {
            override fun onItemClick(documentSnapshot: DocumentSnapshot, position: Int) {
                var guide = documentSnapshot.toObject(Guide::class.java)
                var name = guide!!.name
                var description = guide.description
                var owner = guide.owner
                var userList = guide.usersArray!!.toTypedArray()
                var attractionArray = guide.attractionsArray!!.toTypedArray()
                var time = guide.time!!.toString()

                inflate.findNavController().navigate(GuidesFragmentDirections.actionGuidesFragmentToGuide(userList,attractionArray,name,description, owner, time))

            }

        })

        return inflate
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
