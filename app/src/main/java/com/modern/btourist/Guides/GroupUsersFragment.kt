package com.modern.btourist.Guides


import android.animation.Animator
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.activity.OnBackPressedCallback
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.modern.btourist.Database.User


import com.modern.btourist.R

class GroupUsersFragment : Fragment() {

    private lateinit var adapter: UserAdapter
    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var col: CollectionReference = db.collection("users")
    lateinit var registration: ListenerRegistration

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_group_users, container, false)
        val bundle = GroupUsersFragmentArgs.fromBundle(arguments!!)
        val recycleView = view.findViewById<ListView>(R.id.listview)


        registration = col.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if( firebaseFirestoreException!=null){
                Log.e("UsersGroup","Snapshot listen failed",firebaseFirestoreException)
            }else{
            if(querySnapshot!=null) {
                val list = bundle.userList!!
                var usersList: ArrayList<User> = ArrayList()
                for(u in list){

                    for(document in querySnapshot) {

                        var user = document.toObject(User::class.java)
                        if (user.fullName == u){
                            usersList.add(user)
                        }

                    }
                }

                adapter = UserAdapter(this.context!!,R.layout.user_item,usersList)
                recycleView.adapter = adapter
            }
            }
        }
        return view

    }

    override fun onCreateAnimator(transit: Int, enter: Boolean, nextAnim: Int): Animator? {
        return super.onCreateAnimator(transit, enter, nextAnim)
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
