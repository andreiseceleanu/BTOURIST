package com.modern.btourist.Database

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

object FirestoreUtil {

    private val firebaseFirestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val currentUserDocRef: DocumentReference
    get() = firebaseFirestore.document("users/${FirebaseAuth.getInstance().currentUser?.uid
        ?: throw NullPointerException("UID is null.")} ")

    fun initCurrentUserIfFirstTime(onComplete:  () -> Unit){
         currentUserDocRef.get().addOnSuccessListener { documentSnapshot ->
             if(!documentSnapshot.exists() ){
                  val newUser = User()
                 currentUserDocRef.set(newUser).addOnSuccessListener { onComplete() }
             }
             else onComplete()
         }
    }

    fun updateCurrentUser(firstName: String ="",
                          lastName: String ="",
                          email: String ="",
                          phone: Long =0L,
                          interest1: String ="",
                          interest2: String ="",
                          interest3: String ="",
                          age: Int =0,
                          sex: String ="",
                          language1: String ="",
                          language2: String ="",
                          profilePicturePath: String? = null,
                          latitude: Double =0.0,
                          longitude: Double =0.0,
                          fullName: String=""){

        val userFieldMap = mutableMapOf<String,Any>()
        if(firstName.isNotBlank()) userFieldMap["firstName"]=firstName
        if(lastName.isNotBlank()) userFieldMap["lastName"]=lastName
        if(email.isNotBlank()) userFieldMap["email"]=email
        if(phone!=0L) userFieldMap["phone"]=phone
        if(interest1.isNotBlank()) userFieldMap["interest1"]=interest1
        if(interest2.isNotBlank()) userFieldMap["interest2"]=interest2
        if(interest3.isNotBlank()) userFieldMap["interest3"]=interest3
        if(age!=0) userFieldMap["age"]=age
        if(sex.isNotBlank()) userFieldMap["sex"]=sex
        if(language1.isNotBlank()) userFieldMap["language1"]=language1
        if(language2.isNotBlank()) userFieldMap["language2"]=language2
        if(profilePicturePath!=null) userFieldMap["profilePicturePath"]=profilePicturePath
        if(latitude!=0.0) userFieldMap["latitude"]=latitude
        if(longitude!=0.0) userFieldMap["longitude"]=longitude
        if(fullName!="") userFieldMap["fullName"]=fullName

        currentUserDocRef.update(userFieldMap)
    }

    fun getCurrentUser(onComplete: (User?) -> Unit){
        currentUserDocRef.get().addOnSuccessListener { onComplete(it.toObject(User::class.java))
        }
    }

}