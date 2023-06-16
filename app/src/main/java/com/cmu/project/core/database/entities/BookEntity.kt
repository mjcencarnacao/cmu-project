package com.cmu.project.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cmu.project.core.models.Book

@Entity(tableName = "book")
data class BookEntity(
    @PrimaryKey val id: String,
    val code: Long,
    val title: String,
    val author: String,
    val rating: Float
)

fun BookEntity.toBookModel(): Book {
    return Book(
        id = id,
        title = title,
        author = author,
        rating = rating,
        code = code
    )
}
