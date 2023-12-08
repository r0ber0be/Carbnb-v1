package com.example.carbnb.model

import java.io.Serializable

data class Message(
    val id : String,
    val authorID : String,
    val authorName : String,
    var dateTime : String,
    var content : String,
) : Serializable
