package com.cmu.project.core.models

import com.google.firebase.firestore.GeoPoint

data class Library(var name: String = "", var location: GeoPoint = GeoPoint(1.0, 1.0))
