package com.cmu.project.core.database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cmu.project.core.database.entities.BookEntity

@Dao
interface BookDao {

    @Query("SELECT * FROM book")
    fun getAll(): List<BookEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(bookEntity: BookEntity)

    @Delete
    fun delete(bookEntity: BookEntity)

}