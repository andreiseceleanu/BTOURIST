package com.modern.btourist.Database

data class Attraction(var category: String, var description: String, var latitude: Double, var longitude: Double, var name: String, var phone: Long, var website: String, var image: String){
    constructor():this("","",0.0,0.0, "",0L, "","")
}

