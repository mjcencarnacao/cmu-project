package com.cmu.project.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cmu.project.core.models.Library
import com.google.firebase.firestore.GeoPoint
import com.google.gson.Gson

@Entity(tableName = "library")
data class LibraryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val location: String,
    val rating: Float,
    val bookEntityList: String
)

fun LibraryEntity.toLibrary(): Library {
    return Library(
        id = id,
        name = name,
        location = Gson().fromJson(location, GeoPoint::class.java),
        rating = rating
    )
}