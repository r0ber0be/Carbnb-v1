package com.example.carbnb.model

import java.io.Serializable

data class Advertise (
    val id : String,
    val ownerId : String,
    var date : String,
    var model : String,
    var price : String,
    var location : String,
    var latitude : Double,
    var longitude : Double,
    var description : String?,
    var carImage : String,
    var messages : MutableList<String>?
) : Serializable