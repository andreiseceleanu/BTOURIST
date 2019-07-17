package com.modern.btourist.Map


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore
import com.modern.btourist.Database.FirestoreUtil

import com.modern.btourist.R

class VisitedFragment : Fragment() {

    val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_visited, container, false)

        val listView = view.findViewById<ListView>(R.id.visitedListView)


        FirestoreUtil.getCurrentUser { user ->
            var visitedList = user!!.visitedList.toMutableList()
            var adapter = ArrayAdapter(context,android.R.layout.simple_list_item_1,visitedList)
            listView.adapter = adapter

            db.collection("attractions").get().addOnSuccessListener {
                var i = 0
                for(attraction in it){
                    i++
                }
                var visited = visitedList.size
                var precentage = (100*visited)/i

                var textView = view.findViewById<TextView>(R.id.precentageTextView)
                textView.text = "Bucharest "+precentage+"% visited"
            }
        }



        return view
    }


}
