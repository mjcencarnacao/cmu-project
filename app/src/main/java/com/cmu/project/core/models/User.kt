package com.cmu.project.core.models

import com.cmu.project.core.database.entities.UserEntity
import com.google.firebase.firestore.DocumentReference
import com.google.gson.Gson

data class User(
    val id: String,
    val name: String,
    val email: String,
    val password: String,
    val favourites: List<DocumentReference>,
    val notifications: List<DocumentReference>
)

fun User.toUserEntity(): UserEntity {
    return UserEntity(
        id = id,
        name = name,
        email = email,
        password = password,
        favourites = Gson().toJson(favourites),
        notifications = Gson().toJson(notifications)
    )
}
