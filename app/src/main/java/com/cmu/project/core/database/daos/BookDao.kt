package com.cmu.project.core.database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.cmu.project.core.database.entities.BookEntity

@Dao
interface BookDao {

    @Query("SELECT * FROM book")
    fun getAll(): List<BookEntity>

    @Insert
    fun insertAll(vararg bookEntities: BookEntity)

    @Delete
    fun delete(bookEntity: BookEntity)

}