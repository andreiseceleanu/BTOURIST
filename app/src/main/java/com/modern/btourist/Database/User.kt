package com.modern.btourist.Database

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(var firstName: String,
                var lastName: String,
                var email: String,
                var phone: Long,
                var interest1: String,
                var interest2: String,
                var interest3: String,
                var age: Int,
                var sex: String,
                var language1: String,
                var language2: String,
                var profilePicturePath: String?,
                var latitude: Double,
                var longitude: Double,
                var fullName: String) : Parcelable {

    constructor():this("","","",0L,"","","",0,"","","",null,
        0.0,0.0,"")

}

@Parcelize
class Users: ArrayList<User>(), Parcelable