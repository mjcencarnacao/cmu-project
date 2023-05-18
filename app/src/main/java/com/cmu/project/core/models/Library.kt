package com.cmu.project.core.models

import com.cmu.project.core.Constants.EMPTY_STRING
import com.google.firebase.firestore.GeoPoint

data class Library(
    var name: String = EMPTY_STRING,
    var location: GeoPoint = GeoPoint(0.0, 0.0),
    var rating: Float = 0.0F,
)
