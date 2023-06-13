package com.cmu.project.core.models

import com.cmu.project.core.Constants.EMPTY_STRING
import com.cmu.project.core.database.entities.LibraryEntity
import com.google.firebase.firestore.GeoPoint
import com.google.gson.Gson

data class Library(
    var id: String = EMPTY_STRING,
    var name: String = EMPTY_STRING,
    var location: GeoPoint = GeoPoint(0.0, 0.0),
    var rating: Float = 0.0F,
)

fun Library.toLibraryEntity(): LibraryEntity {
    return LibraryEntity(
        id = id,
        name = name,
        location = Gson().toJson(location),
        rating = rating,
        EMPTY_STRING
    )
}
