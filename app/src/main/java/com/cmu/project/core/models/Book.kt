package com.cmu.project.core.models

import com.cmu.project.core.Constants.EMPTY_STRING

data class Book(
    var id: String = EMPTY_STRING,
    var title: String = EMPTY_STRING,
    var code: Int = -1,
    var author: String = EMPTY_STRING,
    var rating: Float = 0.0F
)
