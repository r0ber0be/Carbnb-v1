package com.example.carbnb.model

import java.io.Serializable

data class User(
    val id: String,
    var name : String,
    var email : String,
    var password : String,
    var profile : String?
    ): Serializable
