package com.modern.btourist.Guides


import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.gson.Gson
import com.modern.btourist.Database.*
import com.modern.btourist.Map.GlideApp
import com.modern.btourist.R
import com.modern.btourist.databinding.FragmentGuideBinding
import java.io.File


class GuideInfo : Fragment() {

    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var col: CollectionReference = db.collection("users")
    private var guideCol: CollectionReference = db.collection("guides")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = DataBindingUtil.inflate<FragmentGuideBinding>(inflater,
            R.layout.fragment_guide, container, false)
        val bundle = GuideInfoArgs.fromBundle(arguments!!)

        /*var query: Query = col.whereEqualTo("fullName", bundle.owner)
        query.get().addOnSuccessListener {
            var user: List<User> = it.toObjects(User::class.java)
            for(u in user){
                var path = u.profilePicturePath
                var ref = StorageUtil.pathToReference(path!!)
                var localFile: File = File.createTempFile("Images", "jpeg")
                ref.getFile(localFile).addOnSuccessListener {
                    val options = BitmapFactory.Options()
                    options.inSampleSize = 8
                    var myImage = BitmapFactory.decodeFile(localFile.absolutePath,options)
                    GlideApp.with(Btourist.instance).load(myImage).placeholder(R.drawable.people).into(view!!.findViewById(R.id.ownerImageView))
                }
            }*/

            val nameText = binding.guideNameTextt
            val descriptionText = binding.guideDescriptionTextt
            val dateText = binding.dateTextViewt
            /*val languageText1 = binding.languageTextView1t
            val languageText2 = binding.languageTextView2t
            val ageText = binding.ageTextViewt
            val sexText = binding.sexTextViewt
            val phoneText = binding.phoneTextViewt
            val interestText1 = binding.interestText1t
            val interestText2 = binding.interestText2t
            val interestText3 = binding.interestText3t
            val ownerText = binding.nameTextViewt*/


            nameText.text = bundle.name
            descriptionText.text = bundle.description
            dateText.text = bundle.time
            //languageText1.text = user.first().language1
            //languageText2.text = user.first().language2
            //ageText.text = user.first().age.toString()
            //sexText.text = user.first().sex
            //phoneText.text = user.first().phone.toString()
            //interestText1.text = user.first().interest1
            //interestText2.text = user.first().interest2
            //interestText3.text = user.first().interest3
            //ownerText.text = user.first().fullName

        //}


        val attractionsButton = binding.attractionsListButtont
        val usersButton = binding.groupButtont
        val joinButton = binding.joinButton

        usersButton.setOnClickListener {
            view!!.findNavController().navigate(GuideInfoDirections.actionGuideToGroupUsersFragment(bundle.userList))
        }

        attractionsButton.setOnClickListener {
            view!!.findNavController().navigate(GuideInfoDirections.actionGuideToGroupAttractionsFragment(bundle.attractionArray))
        }

        joinButton.setOnClickListener {
            var prefs: SharedPreferences = activity!!.getSharedPreferences("com.modern.btourist.prefs",0)
            if(prefs.getBoolean("joined",false)){
                Snackbar.make(binding.root,"You already joined a Group. Exit to join another",Snackbar.LENGTH_LONG).show()
            }else{
            guideCol.whereEqualTo("owner",bundle.owner).get().addOnSuccessListener {
                var guide = it.first()
                var currentUser = FirestoreUtil.getCurrentUser { user ->
                    db.collection("guides").document(guide.id).update("usersArray",FieldValue.arrayUnion(user!!.fullName))

                    var gson = Gson()
                    var editor = prefs.edit()
                    var guideObject = guide.toObject(Guide::class.java)
                    editor.putBoolean("joined",true)
                    editor.putString("guideId",guide.id)
                    editor.putString("attractionSet",gson.toJson(guideObject.attractionsArray!!))
                    editor.putString("owner",guideObject.owner)
                    editor.apply()

                    view!!.findNavController().navigate(GuideInfoDirections.actionGuideToMapFragment(""))
                }

            }
            }

        }

        return binding.root
    }


}
