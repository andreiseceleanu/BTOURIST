package com.modern.btourist.Database

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Guide(var attractionsArray: ArrayList<String>?,var owner: String, var usersArray: ArrayList<String>?,var time: String?, var name:String, var description: String) :
    Parcelable {
    constructor() : this(null, "",null, "","","")

}

@Parcelize
class Guides: ArrayList<Guide>(), Parcelable