package com.modern.btourist.Guides

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
import com.modern.btourist.Database.Btourist
import com.modern.btourist.Database.StorageUtil
import com.modern.btourist.Database.User
import com.modern.btourist.Map.GlideApp
import java.io.File

import android.content.Context


class UserAdapterKotlin : RecyclerView.Adapter<UserAdapterKotlin.ViewHolder> {


    var list: List<User> = ArrayList()
    var context: Context? = null

    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var col: CollectionReference = db.collection("users")

    constructor(userList: List<User>, context: Context){
        this.list = userList
        this.context = context
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View = LayoutInflater.from(parent.context).inflate(com.modern.btourist.R.layout.user_item,parent,false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {

        p0.name.text = list[p1].fullName
        p0.language1.text = list[p1].language1
        p0.language2.text = list[p1].language2
        p0.sex.text = list[p1].sex
        p0.age.text = list[p1].age.toString()
        p0.phone.text = list[p1].phone.toString()
        p0.interest1.text = list[p1].interest1
        p0.interest2.text = list[p1].interest2
        p0.interest3.text = list[p1].interest3

        /*var query: Query = col.whereEqualTo("fullName", list[p1].fullName)
        query.get().addOnSuccessListener {
            var user: List<User> = it.toObjects(User::class.java)
            for(u in user){
                var path = u.profilePicturePath
                var ref = StorageUtil.pathToReference(path!!)
                var localFile: File = File.createTempFile("Images", "jpeg")
                ref.getFile(localFile).addOnSuccessListener {
                    var myImage = BitmapFactory.decodeFile(localFile.absolutePath)
                    GlideApp.with(Btourist.instance).load(myImage).placeholder(com.modern.btourist.R.drawable.people).into(p0.imageView)
                }


            }
        }*/



    }

    inner class ViewHolder : RecyclerView.ViewHolder {

        var imageView: ImageView
        var  name: TextView
        var  language1: TextView
        var  language2: TextView
        var  age: TextView
        var  sex: TextView
        var  phone: TextView
        var  interest1: TextView
        var  interest2: TextView
        var  interest3: TextView

        constructor(itemView: View) : super(itemView){
            imageView = itemView.findViewById(com.modern.btourist.R.id.userImageView)
            name = itemView.findViewById(com.modern.btourist.R.id.fullNameText)
            language1 = itemView.findViewById(com.modern.btourist.R.id.languageText1)
            language2 = itemView.findViewById(com.modern.btourist.R.id.languageText2)
            age = itemView.findViewById(com.modern.btourist.R.id.ageTextView)
            sex = itemView.findViewById(com.modern.btourist.R.id.nameText)
            phone = itemView.findViewById(com.modern.btourist.R.id.phoneText)
            interest1 = itemView.findViewById(com.modern.btourist.R.id.interestText1)
            interest2 = itemView.findViewById(com.modern.btourist.R.id.interestText2)
            interest3 = itemView.findViewById(com.modern.btourist.R.id.interestText3)
        }


    }
    override fun getItemCount(): Int {
        return list.size
    }
}