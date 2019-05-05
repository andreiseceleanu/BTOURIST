package com.modern.btourist.Database

data class User(var firstName: String,
                var lastName: String,
                var email: String,
                var phone: Int,
                var interest1: String,
                var interest2: String,
                var interest3: String,
                var age: Int,
                var sex: String,
                var language1: String,
                var language2: String,
                var profilePicturePath: String?,
                var latitude: Double,
                var longitude: Double){

    constructor():this("","","",0,"","","",0,"","","",null,
        0.0,0.0)

}