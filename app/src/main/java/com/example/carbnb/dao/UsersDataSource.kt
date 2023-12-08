package com.example.carbnb.dao

import com.example.carbnb.model.User

class UsersDataSource {

    companion object{

        fun createUsersList(): ArrayList<User> {
            val list = ArrayList<User>()

            list.add(User(
                "UserID",
                "adm",
                "adm@gmail.com",
                "******",
                null))

            return list
        }

    }
}