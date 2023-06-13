package com.cmu.project.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "book")
data class BookEntity(
    @PrimaryKey val id: String,
    val code: Long,
    val title: String,
    val author: String,
    val rating: Float
)
