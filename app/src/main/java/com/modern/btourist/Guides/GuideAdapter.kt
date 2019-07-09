package com.modern.btourist.Guides

import android.app.Activity
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.modern.btourist.Database.*
import com.modern.btourist.Database.Guide
import com.modern.btourist.Map.GlideApp
import com.modern.btourist.R
import java.io.File

class GuideAdapter : FirestoreRecyclerAdapter<Guide, GuideAdapter.ViewHolder> {
    lateinit var listener: OnItemClickListener
    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var col: CollectionReference = db.collection("users")

    constructor(options: FirestoreRecyclerOptions<Guide>) : super(options){

    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View = LayoutInflater.from(parent.context).inflate(R.layout.guide_item,parent,false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int, p2: Guide) {

        p0.name.text = p2.name
        p0.nowVisiting.text = p2.attractionsArray!![0]
        p0.by.text = "By "+p2.owner
        var query: Query = col.whereEqualTo("fullName", p2.owner)
        query.get().addOnSuccessListener {
            var user: List<User> = it.toObjects(User::class.java)
            for(u in user){
                var path = u.profilePicturePath
                var ref = StorageUtil.pathToReference(path!!)
                var localFile: File = File.createTempFile("Images", "jpeg")
                ref.getFile(localFile).addOnSuccessListener {
                    var myImage = BitmapFactory.decodeFile(localFile.absolutePath)
                    GlideApp.with(Btourist.instance).load(myImage).placeholder(R.drawable.people).into(p0.imageView)
                }


            }
        }


    }

    inner class ViewHolder : RecyclerView.ViewHolder {

        var imageView: ImageView
        var  name: TextView
        var  nowVisiting: TextView
        var  by: TextView

        constructor(itemView: View) : super(itemView){
            imageView = itemView.findViewById(R.id.attractionImage)
            name = itemView.findViewById(R.id.nameTextView)
            nowVisiting = itemView.findViewById(R.id.nowVisitingTextView)
            by = itemView.findViewById(R.id.ownerTextView)

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