package com.cmu.project.core.models

import com.cmu.project.core.Constants.EMPTY_STRING
import com.cmu.project.core.database.entities.BookEntity

data class Book(
    var id: String = EMPTY_STRING,
    var title: String = EMPTY_STRING,
    var code: Long = -1,
    var author: String = "Unknown",
    var rating: Float = 0.0F
)

fun Book.toBookEntity(): BookEntity {
    return BookEntity(id = id, title = title, code = code, author = author, rating = rating)
}
