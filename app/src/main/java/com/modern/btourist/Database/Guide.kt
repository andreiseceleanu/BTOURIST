package com.modern.btourist.Database

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Guide(var attractionsArray: ArrayList<String>?,var owner: String, var usersArray: ArrayList<String>?,var time: Timestamp?, var name:String, var description: String) :
    Parcelable {
    constructor() : this(null, "",null, null,"","")

}

@Parcelize
class Guides: ArrayList<Guide>(), Parcelable