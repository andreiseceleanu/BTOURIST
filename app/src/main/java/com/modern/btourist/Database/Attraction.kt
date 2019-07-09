package com.modern.btourist.Database

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Attraction(var category: String, var description: String, var latitude: Double, var longitude: Double, var name: String, var phone: Long, var website: String, var image: String) :
    Parcelable {
    constructor():this("","",0.0,0.0, "",0L, "","")
}

@Parcelize
class Attractions: ArrayList<Attraction>(), Parcelable