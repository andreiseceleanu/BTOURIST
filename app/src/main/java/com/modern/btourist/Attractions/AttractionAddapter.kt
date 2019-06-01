package com.modern.btourist.Attractions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.modern.btourist.Database.Attraction
import com.modern.btourist.Database.Btourist
import com.modern.btourist.R
import kotlinx.android.synthetic.main.attraction_item.view.*

class AttractionAddapter : FirestoreRecyclerAdapter<Attraction, AttractionAddapter.ViewHolder>{
    lateinit var listener: OnItemClickListener

    constructor(options: FirestoreRecyclerOptions<Attraction>) : super(options){

    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View = LayoutInflater.from(parent.context).inflate(R.layout.attraction_item,parent,false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int, p2: Attraction) {
        fun getImage(imageName: String): Int {

            var app = Btourist.instance.applicationContext

            var drawableResourceId = app.resources.getIdentifier(imageName, "drawable", app.packageName)

            return drawableResourceId
        }
        var image = p2.image
        var resource = getImage(image)
        p0.imageView.setImageResource(resource)
        p0.name.text = p2.name
        p0.category.text = p2.category

    }

    inner class ViewHolder : RecyclerView.ViewHolder {

        var imageView: ImageView
        var  name: TextView
        var  category: TextView

        constructor(itemView: View) : super(itemView){
            imageView = itemView.findViewById(R.id.attractionImage)
            name = itemView.findViewById(R.id.nameTextView)
            category = itemView.findViewById(R.id.categoryTextView)

            itemView.setOnClickListener {
                var position: Int = adapterPosition
                if(position != RecyclerView.NO_POSITION && listener != null)
                {
                    listener.onItemClick(snapshots.getSnapshot(position),position)
                }
            }

        }


    }
    interface  OnItemClickListener{
        fun onItemClick(documentSnapshot: DocumentSnapshot, position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener){
        this.listener = listener
    }
}